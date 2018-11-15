package autoo;

import static org.objectweb.asm.Opcodes.*;

import java.util.HashMap;
import java.util.Map;

class TypeData {
    private static Map<Class<?>,TypeData> dataForType = new HashMap<>();
    static {
        dataForType.put(Byte.TYPE, 
                new TypeData(1, ILOAD, IRETURN, IF_ICMPNE, -1, "put", "(B)V", "getByte", "()B", "(I)",
                        NOP, "publishByte", "(Ljava/lang/String;B)V"));
        dataForType.put(Character.TYPE, 
                new TypeData(1, ILOAD, IRETURN, IF_ICMPNE, -1, "putChar", "(C)V", "getChar", "()C", "(C)", 
                        NOP, "publishChar", "(Ljava/lang/String;C)V"));
        dataForType.put(Short.TYPE, 
                new TypeData(1, ILOAD, IRETURN, IF_ICMPNE, -1, "putShort", "(S)V", "getShort", "()S", "(I)",
                        NOP, "publishShort", "(Ljava/lang/String;S)V"));
        dataForType.put(Integer.TYPE, 
                new TypeData(1, ILOAD, IRETURN, IF_ICMPNE, -1, "putInt", "(I)V", "getInt", "()I", "(I)",
                        NOP, "publishInt", "(Ljava/lang/String;I)V"));
        dataForType.put(Long.TYPE, 
                new TypeData(2, LLOAD, LRETURN, -1, LCMP, "putLong", "(J)V", "getLong", "()J", "(J)",
                        L2I, "publishLong", "(Ljava/lang/String;J)V"));
        dataForType.put(Float.TYPE, 
                new TypeData(1, FLOAD, FRETURN, -1, FCMPL, "putFloat", "(F)V", "getFloat", "()F", "(F)", 
                        F2I, "publishFloat", "(Ljava/lang/String;F)V"));
        dataForType.put(Double.TYPE, 
                new TypeData(2, DLOAD, DRETURN, -1, DCMPL, "putDouble", "(D)V", "getDouble", "()D", "(D)",
                        D2I, "publishDouble", "(Ljava/lang/String;D)V"));
        dataForType.put(String.class,
                new TypeData(1, ALOAD, ARETURN, -1, -1, null, null, null, null, "(Ljava/lang/String;)",
                        INVOKEVIRTUAL, "publish", "(Ljava/lang/String;Ljava/lang/Object;)V"));
        dataForType.put(Object.class, 
                new TypeData(1, ALOAD, ARETURN, -1, -1, null, null, null, null, "(Ljava/lang/Object;)", 
                        INVOKEVIRTUAL, "publish", "(Ljava/lang/String;Ljava/lang/Object;)V"));
        dataForType.put(Void.TYPE, 
                new TypeData(0, NOP, RETURN, -1, -1, null, null, null, null, null, -1, null, null));
    }
    
    static TypeData getTypeData(Class<?> type) {
        TypeData td = dataForType.get(type);
        if (td == null) {
            td = dataForType.get(Object.class);
        }
        return td;
    }

    private TypeData(int size, int loadInsn, int returnInsn, int ifCmpNe, int cmp, String putName, String putDesc,
            String getName, String getDesc, String sbArgDesc, int toIinsn, String publishName, String publishDesc) {
        this.size = size;
        this.loadInsn = loadInsn;
        this.returnInsn = returnInsn;
        this.ifCmpNe = ifCmpNe;
        this.cmp = cmp;
        this.putName = putName;
        this.putDesc = putDesc;
        this.getName = getName;
        this.getDesc = getDesc;
        this.sbArgDesc = sbArgDesc;
        this.toIinsn = toIinsn;
        this.publishName = publishName;
        this.publishDesc = publishDesc;
    }
    int size;
    int loadInsn;
    int returnInsn;
    int cmp;
    int ifCmpNe;
    String putName;
    String putDesc;
    String getName;
    String getDesc;
    String sbArgDesc;
    int toIinsn;
    String publishName;
    String publishDesc;
}