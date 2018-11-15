package autoo;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.DUP2;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IADD;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.IF_ACMPEQ;
import static org.objectweb.asm.Opcodes.IMUL;
import static org.objectweb.asm.Opcodes.INSTANCEOF;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.NOP;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.POP2;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_5;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import autoo.event.Publisher;

class Compiler<T> {

    private static final Type OBJECT_TYPE = Type.getType(Object.class);
    private static final Type OBJECT_ARRAY_TYPE = Type.getType(Object[].class);
    private static final String OBJECT_NAME = OBJECT_TYPE.getInternalName();
    private static final Type CLASS_TYPE = Type.getType(Class.class);
    private static final Type PUBLISHER_TYPE = Type.getType(Publisher.class);
    private static final String PUBLISHER_NAME = PUBLISHER_TYPE.getInternalName();
    private static final Type META_FACTORY_TYPE = Type.getType(MetaFactory.class);
    private static final String META_FACTORY_NAME = META_FACTORY_TYPE.getInternalName();
    private static final Type FACTORY_TYPE = Type.getType(Factory.class);
    private static final String FACTORY_NAME = FACTORY_TYPE.getInternalName();

    // Factory MetaFactory.getFactory(Class c)
    private static final String META_FACTORY_GET_FACTORY_METHOD = "getFactory";
    private static final String META_FACTORY_GET_FACTORY_METHOD_DESC =
        Type.getMethodDescriptor(FACTORY_TYPE, new Type[] { CLASS_TYPE });
    
    // Object Factory.create(Object... args)
    private static final String FACTORY_CREATE_METHOD = "create";
    private static final String FACTORY_CREATE_METHOD_DESC =
        Type.getMethodDescriptor(OBJECT_TYPE, new Type[] { OBJECT_ARRAY_TYPE });
    
    private static final String PUBLISHER_FIELD_NAME = "$publisher";
    
    private Factory<T> _factory;
    private String _instanceClassName;
    private Class<T> _instanceClass;
    private Constructor<T> _constructor;
    private DynamicClassLoader _dcl;
    private String _codecClassName;
    private List<Factory<?>> _factoriesFromTop;
    private String _shortName;
    private List<AttributeType> _allConstructorAttributes = new ArrayList<AttributeType>();
    private List<AttributeType> _allNonConstructorAttributes = new ArrayList<AttributeType>();
    private List<AttributeType> _allAttributes = new ArrayList<AttributeType>();
    private String _constructorDesc;

    Compiler(Factory<T> factory) {
        _factory = factory;
        Class targetClass = _factory.getTargetClass();
        int dot = targetClass.getName().lastIndexOf('.');
        if (dot >= 0) {
            _shortName = targetClass.getName().substring(dot + 1);
        } else {
            _shortName = targetClass.getName();
        }
        _codecClassName = _shortName + "$Codec" + DynamicClassLoader.getNextId();
        _instanceClassName = _shortName + "$Instance" + DynamicClassLoader.getNextId();
        _dcl = DynamicClassLoader.getDynamicClassLoader(_factory.getTargetClass().getClassLoader());
        _factoriesFromTop = new LinkedList<>();
        for (Factory<?> f = factory; f != null; f = f.getParentFactory()) {
            _factoriesFromTop.add(0, f);
        }
        for (Factory<?> f : _factoriesFromTop) {
            for (AttributeType attr : f.getConstructorAttributes()) {
                _allConstructorAttributes .add(attr);        
            }
            for (AttributeType attr : f.getAttributes()) {
                _allAttributes.add(attr);
                if (!f.getConstructorAttributes().contains(attr)) {
                    _allNonConstructorAttributes.add(attr);
                }
            }
        }
        _constructorDesc = Type.getMethodDescriptor(Type.VOID_TYPE,
                attributesToTypeArray(_allConstructorAttributes));
    }
    

    @SuppressWarnings("unchecked")
    Class<T> getInstanceClass() {
        if (_instanceClass == null) {
            byte[] bytes = compileInstanceClass();
            _instanceClass = (Class<T>) _dcl.defineDynamicClass(_instanceClassName, bytes);
        }
        return _instanceClass;
    }
    
    private void compileCodecConstructor(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, OBJECT_NAME, "<init>", "()V");
        mv.visitInsn(RETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();
    }
    
    private byte[] compileInstanceClass() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        List<String> interfaces = new ArrayList<String>();
        Class targetClass = _factory.getTargetClass();
        interfaces.add(Type.getInternalName(targetClass));
        cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, _instanceClassName,
                null, Type.getInternalName(_factory.getImplementationClass()),
                interfaces.toArray(new String[interfaces.size()]));
        compileInstanceFields(cw);
        compileClassInit(cw);
        compileInstanceConstructor(cw);
        compileInstanceGetters(cw);
        compileInstanceSetters(cw);
        compileInstanceBuiltins(cw);
        cw.visitEnd();
        return cw.toByteArray();
    }
    
    private void compileClassInit(ClassWriter cw) {
        if (_factory.hasAutoId()) {
            MethodVisitor mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
            mv.visitCode();
            mv.visitTypeInsn(NEW, "java/util/concurrent/atomic/AtomicLong");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J");
            mv.visitMethodInsn(INVOKESPECIAL, "java/util/concurrent/atomic/AtomicLong", "<init>", "(J)V");
            mv.visitFieldInsn(PUTSTATIC, _instanceClassName, "$nextId", 
                "Ljava/util/concurrent/atomic/AtomicLong;");
            mv.visitInsn(RETURN);
            mv.visitMaxs(4, 0);
            mv.visitEnd();
        }
    }

    private void compileInstanceFields(ClassWriter cw) {
        if (_factory.hasAutoId()) {
            FieldVisitor fv = cw.visitField(ACC_PUBLIC|ACC_FINAL|ACC_STATIC, "$nextId",
                    "Ljava/util/concurrent/atomic/AtomicLong;", null, null);
            fv.visitEnd();
        }
        if (_factory.isPublishable()) {
            FieldVisitor fv = cw.visitField(ACC_PUBLIC, PUBLISHER_FIELD_NAME,
                    PUBLISHER_TYPE.getDescriptor(), null, null);
            fv.visitEnd();
        }
        for (AttributeType attr : _allAttributes) {
            FieldVisitor fv = cw.visitField(ACC_PUBLIC, attr.getName(),
                    Type.getDescriptor(attr.getDeclaredType()), null, null);
            fv.visitEnd();
        }
    }
    
    private Type[] classesToTypeArray(Collection<Class> classes) {
        Type[] result = new Type[classes.size()];
        int i = 0;
        for (Class clss : classes) {
            result[i++] = Type.getType(clss);
        }
        return result;
    }
    
    private Type[] attributesToTypeArray(Collection<AttributeType> attrs) {
        Type[] result = new Type[attrs.size()];
        int i = 0;
        for (AttributeType attr : attrs) {
            result[i++] = Type.getType(attr.getDeclaredType());
        }
        return result;
    }

    private void compileInstanceConstructor(ClassWriter cw) {
        // <init>(<parent args>, <this args>)
        List<Class> ctorFormals = new ArrayList<Class>();
        int locals = 1;
        for (AttributeType attr : _allConstructorAttributes) {
            TypeData td = TypeData.getTypeData(attr.getDeclaredType());
            locals += td.size;
            ctorFormals.add(attr.getDeclaredType());
        }
        String methodDescr = Type.getMethodDescriptor(Type.VOID_TYPE, classesToTypeArray(ctorFormals));
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", methodDescr, null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(_factory.getImplementationClass()),
                "<init>", "()V");
        // initialize publisher
        if (_factory.isPublishable()) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitTypeInsn(NEW, PUBLISHER_NAME);
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, PUBLISHER_NAME, "<init>", "()V");
            mv.visitFieldInsn(PUTFIELD, _instanceClassName, PUBLISHER_FIELD_NAME,
                    PUBLISHER_TYPE.getDescriptor());
        }
        int i = 1;
        // arguments get stuffed into slots
        for (AttributeType attr : _allConstructorAttributes) {
            TypeData insns = TypeData.getTypeData(attr.getDeclaredType());
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(insns.loadInsn, i++);
            mv.visitFieldInsn(PUTFIELD, _instanceClassName, attr.getName(),
                    Type.getDescriptor(attr.getDeclaredType()));
        }
        // create initial values
        for (AttributeType attr : _allNonConstructorAttributes) {
            if (attr.isCollection()) {
                mv.visitVarInsn(ALOAD, 0);
                if (_factory.hasImplementation("init" + attr.getName(), new Class[0])) {
                    mv.visitInsn(DUP);
                    mv.visitMethodInsn(INVOKEVIRTUAL,
                            Type.getInternalName(_factory.getImplementationClass()),
                            "init" + attr.getName(),
                            Type.getMethodDescriptor(Type.getType(attr.getDeclaredType()),
                                    new Type[0]));
                } else if (attr.isAutoObject()) {
                    Type declaredType = Type.getType(attr.getDeclaredType()); 
                    mv.visitLdcInsn(declaredType);
                    mv.visitMethodInsn(INVOKESTATIC, META_FACTORY_NAME, META_FACTORY_GET_FACTORY_METHOD,
                            META_FACTORY_GET_FACTORY_METHOD_DESC);
                    mv.visitInsn(ACONST_NULL);
                    mv.visitMethodInsn(INVOKEVIRTUAL, FACTORY_NAME, FACTORY_CREATE_METHOD,
                            FACTORY_CREATE_METHOD_DESC);
                    mv.visitTypeInsn(CHECKCAST, declaredType.getInternalName());
                } else {
                    String collectionName = Type.getInternalName(attr.getActualType()); 
                    mv.visitTypeInsn(NEW, collectionName);
                    mv.visitInsn(DUP);
                    mv.visitMethodInsn(INVOKESPECIAL, collectionName, "<init>", "()V");
                }
                mv.visitFieldInsn(PUTFIELD, _instanceClassName, attr.getName(), 
                        Type.getDescriptor(attr.getDeclaredType()));
            } else if (attr.isAutoId()) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETSTATIC, _instanceClassName, "$nextId",
                        Type.getDescriptor(AtomicLong.class));
                mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(AtomicLong.class),
                        "incrementAndGet", "()J");
                mv.visitFieldInsn(PUTFIELD, _instanceClassName, attr.getName(), 
                        Type.getDescriptor(attr.getDeclaredType()));
            }
        }
        mv.visitInsn(RETURN);
        mv.visitMaxs(5, locals);
        mv.visitEnd();
    }
        
    private void compileInstanceGetters(ClassWriter cw) {
        for (AttributeType attr : _allAttributes) {
            TypeData td = TypeData.getTypeData(attr.getDeclaredType());
            Type attrType = Type.getType(attr.getDeclaredType());
            String getterDesc = Type.getMethodDescriptor(attrType, new Type[0]);
            MethodVisitor mv =
                cw.visitMethod(ACC_PUBLIC, "get" + attr.getName(), getterDesc, null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, _instanceClassName, attr.getName(), attrType.getDescriptor());
            mv.visitInsn(td.returnInsn);
            mv.visitMaxs(td.size, td.size);
            mv.visitEnd();
        }
    }
    
    private void compileInstanceSetters(ClassWriter cw) {
        for (AttributeType attr : _allAttributes) {
            if (!attr.isWritable()) {
                continue;
            }
            TypeData td = TypeData.getTypeData(attr.getDeclaredType());
            Type attrType = Type.getType(attr.getDeclaredType());
            String setterDesc = Type.getMethodDescriptor(Type.VOID_TYPE, new Type[] { attrType });
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "set" + attr.getName(), setterDesc, null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(td.loadInsn, 1);
            mv.visitFieldInsn(PUTFIELD, _instanceClassName, attr.getName(),
                    attrType.getDescriptor());
            if (_factory.isPublishable()) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, _instanceClassName, PUBLISHER_FIELD_NAME,
                        PUBLISHER_TYPE.getDescriptor());
                mv.visitLdcInsn(attr.getName());
                mv.visitVarInsn(td.loadInsn, 1);
                mv.visitMethodInsn(INVOKEVIRTUAL, PUBLISHER_NAME, td.publishName, td.publishDesc); 
            }
            mv.visitInsn(RETURN);
            mv.visitMaxs(2 + td.size, 1 + td.size);
            mv.visitEnd();
        }
    }

    Constructor<T> getInstanceConstructor() {
        if (_constructor == null) {
            List<Class> types = new ArrayList<Class>();
            for (Factory<?> factory : _factoriesFromTop) {
                for (AttributeType attr : factory.getConstructorAttributes()) {
                    types.add(attr.getDeclaredType());
                }                
            }
            Class<T> clss = getInstanceClass();
            try {
                _constructor = clss.getConstructor(types.toArray(new Class[types.size()]));
            } catch (Exception e) {
                throw AoException.rethrow.wrap("could not find constructor", e);
            }
        }
        return _constructor;
    }

    private void compileInstanceBuiltins(ClassWriter cw) {
        if (_allConstructorAttributes.size() > 0) {
            compileInstanceEquals(cw);
            compileInstanceHashCode(cw);
        }
        if (_factory.isPublishable()) {
            compileInstancePublishChangesDelegate(cw);
            compileInstancePublisherDelegate(cw, "addListener");
            compileInstancePublisherDelegate(cw, "removeListener");
        }
        compileInstanceToString(cw);
    }

    private void compileInstancePublishChangesDelegate(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "publishChanges", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, _instanceClassName, PUBLISHER_FIELD_NAME, 
                PUBLISHER_TYPE.getDescriptor());
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, PUBLISHER_NAME, "publishChanges", "(Ljava/lang/Object;)V");
        mv.visitInsn(RETURN);
        mv.visitMaxs(2, 1);
        mv.visitEnd();
    }
    
    private void compileInstancePublisherDelegate(ClassWriter cw, String name) {
        MethodVisitor mv = 
            cw.visitMethod(ACC_PUBLIC, name, "(Lcom/metopic/util/IListener;)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, _instanceClassName, PUBLISHER_FIELD_NAME,
                PUBLISHER_TYPE.getDescriptor());
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, PUBLISHER_NAME, name, 
                "(Lcom/metopic/util/IListener;)V");
        mv.visitInsn(RETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();
    }

    private void compileInstanceEquals(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "equals",
                Type.getMethodDescriptor(Type.BOOLEAN_TYPE, new Type[] { OBJECT_TYPE }), null, null);
        mv.visitCode();
        
        Label returnFalse = new Label();
        Label returnTrue = new Label();
        
        // not null and same type
        mv.visitVarInsn(ALOAD, 1);
        mv.visitJumpInsn(IFNULL, returnFalse);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(INSTANCEOF, Type.getInternalName(_factory.getTargetClass()));
        mv.visitJumpInsn(IFEQ, returnFalse);
        
        // same object
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitJumpInsn(IF_ACMPEQ, returnTrue);

        compileInstanceEqualsCompareAttributes(mv, returnFalse, returnTrue);
        
        mv.visitLabel(returnTrue);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(IRETURN);
        
        mv.visitLabel(returnFalse);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(IRETURN);
        mv.visitMaxs(7,3);
        mv.visitEnd();
    }

    private void compileInstanceEqualsCompareAttributes(MethodVisitor mv, Label returnFalse, Label returnTrue) {
        Label pop2ReturnFalse = new Label();
        Label pop3ReturnFalse = new Label();
        
        // cast
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(CHECKCAST, _instanceClassName);
        mv.visitVarInsn(ASTORE, 2);
      
        // compare only immutable attributes
        for (AttributeType attr : _allConstructorAttributes) {
            TypeData td = TypeData.getTypeData(attr.getDeclaredType());

            // push 2 attribute values
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, _instanceClassName, attr.getName(),
                        Type.getDescriptor(attr.getDeclaredType()));
            mv.visitVarInsn(ALOAD, 2);
            mv.visitFieldInsn(GETFIELD, _instanceClassName, attr.getName(), 
                        Type.getDescriptor(attr.getDeclaredType()));
            
            if (td.ifCmpNe != -1) {
                mv.visitJumpInsn(td.ifCmpNe, returnFalse);
            } else if (td.cmp != -1) {
                mv.visitInsn(td.cmp);
                mv.visitJumpInsn(IFNE, returnFalse);
            } else {
                Label pop2NextAttr = new Label();
                Label nextAttr = new Label();
                // if x == o.x then next
                mv.visitInsn(DUP2);
                mv.visitJumpInsn(IF_ACMPEQ, pop2NextAttr);
                // if x == null || o.x == null then return false
                mv.visitInsn(DUP2);
                mv.visitJumpInsn(IFNULL, pop3ReturnFalse);
                mv.visitJumpInsn(IFNULL, pop2ReturnFalse);
                // if !x.equals(o.x) return false
                mv.visitMethodInsn(INVOKEVIRTUAL,
                        Type.getInternalName(attr.getDeclaredType()), "equals", 
                        Type.getMethodDescriptor(Type.BOOLEAN_TYPE, new Type[] { OBJECT_TYPE }));
                mv.visitJumpInsn(IFEQ, returnFalse);
                mv.visitJumpInsn(GOTO, nextAttr);
                
                mv.visitLabel(pop2NextAttr);
                mv.visitInsn(POP2);
                mv.visitLabel(nextAttr);
            }
        }
        mv.visitJumpInsn(GOTO, returnTrue);
        
        mv.visitLabel(pop3ReturnFalse);
        mv.visitInsn(POP);
        mv.visitLabel(pop2ReturnFalse);
        mv.visitInsn(POP2);
        mv.visitJumpInsn(GOTO, returnFalse);
    }

    private void compileInstanceToString(ClassWriter cw) {
        MethodVisitor mv =
            cw.visitMethod(ACC_PUBLIC, "toString", "()Ljava/lang/String;", null, null);
        mv.visitCode();
        String stringBuilder = Type.getInternalName(StringBuilder.class);
        mv.visitTypeInsn(NEW, stringBuilder);
        mv.visitInsn(DUP);
        mv.visitLdcInsn(_shortName + "{");
        mv.visitMethodInsn(INVOKESPECIAL, stringBuilder, "<init>", "(Ljava/lang/String;)V");
        boolean first = true;
        for (AttributeType attr : _allAttributes) {
            String name;
            if (first) {
                name = attr.getName() + "=";
                first = false;
            } else {
                name = " " + attr.getName() + "=";
            }
            mv.visitLdcInsn(name);
            mv.visitMethodInsn(INVOKEVIRTUAL, stringBuilder, "append",
                    "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, _instanceClassName, attr.getName(), 
                    Type.getDescriptor(attr.getDeclaredType()));
            TypeData td = TypeData.getTypeData(attr.getDeclaredType());
            String appendDesc = td.sbArgDesc + "Ljava/lang/StringBuilder;";
            mv.visitMethodInsn(INVOKEVIRTUAL, stringBuilder, "append", appendDesc);
        }
        mv.visitLdcInsn("}");
        mv.visitMethodInsn(INVOKEVIRTUAL, stringBuilder, "append",
                "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
        mv.visitMethodInsn(INVOKEVIRTUAL, stringBuilder, "toString", "()Ljava/lang/String;");
        mv.visitInsn(ARETURN);
        mv.visitMaxs(4,1);
        mv.visitEnd();
    }
    
    private void compileInstanceHashCode(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "hashCode", "()I", null, null);
        mv.visitCode();
        mv.visitInsn(ICONST_0);
        boolean doMul37 = false;
        for (AttributeType attr : _allConstructorAttributes) {
            if (doMul37) {
                mv.visitIntInsn(BIPUSH, 37);
                mv.visitInsn(IMUL);
            }
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, _instanceClassName, attr.getName(), 
                    Type.getDescriptor(attr.getDeclaredType()));
            TypeData td = TypeData.getTypeData(attr.getDeclaredType());
            if (td.toIinsn == NOP) {
                // do nothing
            } else if (td.toIinsn == INVOKEVIRTUAL) {
                mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(attr.getDeclaredType()),
                        "hashCode", "()I");
            } else {
                mv.visitInsn(td.toIinsn);
            }
            mv.visitInsn(IADD);
            doMul37 = true;
        }
        mv.visitInsn(IRETURN);
        mv.visitMaxs(3, 2);
        mv.visitEnd();
    }


}
