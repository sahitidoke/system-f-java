public class Eval {

    public boolean isValue(
            Term term) {

        return term instanceof Abs
                || term instanceof TypeAbs;
    }

    public Term step(
            Term term) {

        if (term instanceof App app) {

            if (!isValue(app.function())) {

                return new App(
                        step(app.function()),
                        app.argument());
            }

            if (!isValue(app.argument())) {

                return new App(
                        app.function(),
                        step(app.argument()));
            }

            if (app.function() instanceof Abs abs) {

                return TermSub.substitute(
                        abs.body(),
                        abs.param(),
                        app.argument());
            }
        }

        if (term instanceof TypeApp ta) {

            if (!isValue(ta.term())) {

                return new TypeApp(
                        step(ta.term()),
                        ta.typeArgument());
            }

            if (ta.term() instanceof TypeAbs abs) {

                return TypeTermSub.substitute(
                        abs.body(),
                        abs.typeVar(),
                        ta.typeArgument());
            }
        }

        throw new RuntimeException(
                "no evaluation rule");
    }

    public Term eval(
            Term term) {

        while (true) {

            try {
                term = step(term);
            } catch (RuntimeException e) {
                return term;
            }
        }
    }
}
