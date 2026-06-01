public final class TermSub {

    private TermSub() {
    }

    public static Term substitute(
            Term term,
            String variable,
            Term replacement) {

        if (term instanceof Var v) {

            if (v.name().equals(variable)) {
                return replacement;
            }

            return v;
        }

        if (term instanceof Abs abs) {

            if (abs.param().equals(variable)) {
                return abs;
            }

            return new Abs(
                    abs.param(),
                    abs.paramType(),
                    substitute(
                            abs.body(),
                            variable,
                            replacement));
        }

        if (term instanceof App app) {

            return new App(
                    substitute(
                            app.function(),
                            variable,
                            replacement),
                    substitute(
                            app.argument(),
                            variable,
                            replacement));
        }

        if (term instanceof TypeAbs ta) {

            return new TypeAbs(
                    ta.typeVar(),
                    substitute(
                            ta.body(),
                            variable,
                            replacement));
        }

        if (term instanceof TypeApp ta) {

            return new TypeApp(
                    substitute(
                            ta.term(),
                            variable,
                            replacement),
                    ta.typeArgument());
        }

        throw new RuntimeException(
                "undefined term");
    }
}