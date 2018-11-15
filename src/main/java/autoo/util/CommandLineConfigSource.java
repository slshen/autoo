package autoo.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CommandLineConfigSource implements ConfigSource {
    
    private static class Flag {
        String name;
        String configName;
        boolean isRequired;
        boolean isSwitch;
        String dflt;
        boolean append;
        Flag(String name, String configName, boolean required, boolean isSwitch, String dflt, boolean append) {
            this.name = name;
            this.configName = configName;
            this.isRequired = required;
            this.isSwitch = isSwitch;
            this.dflt = dflt;
            this.append = append;
        }
        public String toString() {
            String special;
            if (isRequired || isSwitch || append) {
                special = ("(" + (isRequired ? " required" : "")
                               + (isSwitch ? " switch" : "")
                               + (append ? " multi" : "") + ")");
            } else {
                special = "";
            }
            return "-" + name + " sets " + configName + special + " default " + dflt;
        }
    }
    private static class ParseException extends Exception {
        private static final long serialVersionUID = 1L;
        ParseException(String message) {
            super(message);
        }
    }
    private List<Flag> _flags = new ArrayList<Flag>();
    private String[] _args;
    private Map<String,String> _values = new HashMap<String,String>();
    private String _extraArgsName;
    
    public CommandLineConfigSource(String[] args) {
        _args = args;
    }
    
    public boolean parse() {
        try {
            // parse flags
            int i;
            for (i = 0; i < _args.length; i++) {
                if (_args[i].startsWith("-")) {
                    boolean found = false;
                    for (Iterator<Flag> iter = _flags.iterator(); iter.hasNext();) {
                        Flag flag = (Flag) iter.next();
                        if (_args[i].length() == flag.name.length() + 1 
                                && _args[i].equals("-" + flag.name)) {
                            found = true;
                            if (flag.isSwitch) {
                                _values.put(flag.configName, "true");
                            } else if ((i + 1) < _args.length) {
                                if (flag.append && _values.containsKey(flag.configName)) {
                                    String value = _values.get(flag.configName);
                                    _values.put(flag.configName, value + "," + _args[++i]);
                                } else {
                                    _values.put(flag.configName, _args[++i]);
                                }
                            } else {
                                throw new ParseException("flag -" + flag.name + " requires an argument");
                            }
                            break;
                        }
                    }
                    if (!found) {
                        throw new ParseException("unknown flag " + _args[i]);
                    }
                } else {
                    break;
                }
            }
            // check that all required flags have been supplied
            for (Iterator<Flag> iter = _flags.iterator(); iter.hasNext();) {
                Flag flag = (Flag) iter.next();
                if (flag.isRequired) {
                    if (_values.get(flag.configName) == null) {
                        throw new ParseException("flag -" + flag.name + " must be specified");
                    }
                } else if (_values.get(flag.configName) == null && flag.dflt != null) {
                    _values.put(flag.configName, flag.dflt);
                }
            }
            // add extra arguments
            if (i != _args.length) {
                if (_extraArgsName == null) {
                    throw new ParseException("too many arguments");
                } else {
                    _values.put(_extraArgsName + ".count", String.valueOf(_args.length - i));
                    int n = 0;
                    while (i < _args.length) {
                        _values.put(_extraArgsName + "." +  n, _args[i]);
                        i++;
                        n++;
                    }
                }
            }
            return true;
        } catch (ParseException pe) {
            System.err.println(pe.getMessage());
            return false;
        }
        
    }
    
    public void addFlag(String name, String configName, boolean required, String dflt) {
        addFlag(name, configName, required, dflt, false);
    }
    
    public void addFlag(String name, String configName, boolean required, String dflt, boolean append) {
        _flags.add(new Flag(name, configName, required, false, dflt, append));
    }
    
    public void addSwitch(String name, String configName, boolean required) {
        _flags.add(new Flag(name, configName, required, true, "false", false));
    }
    
    public void setExtraArgsName(String name) {
        _extraArgsName = name;
    }

    public String get(String key) {
        return (String) _values.get(key);
    }

    public boolean hasKey(String key) {
        return _values.get(key) != null;
    }

}
