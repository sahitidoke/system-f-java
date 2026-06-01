public final class TypeTermSub {

    private TypeTermSub() {
    }

    public static Term substitute(
            Term term,
            String variable,
            Type replacement) {

        if (term instanceof Var v) {
            return v;
        }

        if (term instanceof Abs abs) {

            return new Abs(
                    abs.param(),
                    TypeSub.substitute(
                            abs.paramType(),
                            variable,
                            replacement),
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

            if (ta.typeVar().equals(variable)) {
                return ta;
            }

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
                    TypeSub.substitute(
                            ta.typeArgument(),
                            variable,
                            replacement));
        }

        throw new RuntimeException(
                "undefined term");
    }
}