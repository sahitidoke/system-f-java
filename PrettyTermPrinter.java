public class PrettyTermPrinter {
 public String print(Term t) {

        if (t instanceof Var v) {
            return v.name();
        }

        if (t instanceof Abs a) {
            return "λ" + a.param() + ":" +
                    printType(a.paramType()) +
                    "." + print(a.body());
        }

        if (t instanceof App app) {
            return "(" + print(app.function()) + " " + print(app.argument()) + ")";
    }

        if (t instanceof TypeAbs ta) {
            return "Λ" + ta.typeVar() + "." + print(ta.body());
        }

        if (t instanceof TypeApp ta) {
            String inner = ta.term() instanceof TypeAbs || ta.term() instanceof Abs || ta.term() instanceof App
            ? "(" + print(ta.term()) + ")"
            : print(ta.term());
            return inner + "[" + printType(ta.typeArgument()) + "]";
        }

        throw new RuntimeException("undefined term");
    }

        private String printType(Type t) {
            return new PrettyTypePrinter().print(t);
        }
    }