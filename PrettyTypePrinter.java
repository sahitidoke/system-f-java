public class PrettyTypePrinter {

    public String print(Type t) {
        return print(t, false);
    }

    private String print(Type t, boolean isRightOfArrow) {

        if (t instanceof TypeVar v) {
            return v.name();
        }

        if (t instanceof ForAllType fa) {
            return "∀" + fa.typeVar() + "." + print(fa.body());
        }

        if (t instanceof FunctionType ft) {
            String left = print(ft.from(), true);
            String right = print(ft.to(), false);

            String result = left + "→" + right;

            if (isRightOfArrow) {
                return "("+result+")";
            }

            return result;
        }

        throw new RuntimeException("undefined type: " + t);
    }
}