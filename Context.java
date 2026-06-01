import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class Context {

    private final Map<String, Type> termBindings =
            new HashMap<>();

    private final Set<String> typeBindings =
            new HashSet<>();

    public void addTerm(String name, Type type) {
        termBindings.put(name, type);
    }

    public void addType(String name) {
        typeBindings.add(name);
    }

    public Type lookupTerm(String name) {
        return termBindings.get(name);
    }

    public boolean containsType(String name) {
        return typeBindings.contains(name);
    }

    public Context copy() {
        Context c = new Context();
        c.termBindings.putAll(termBindings);
        c.typeBindings.addAll(typeBindings);
        return c;
    }
}