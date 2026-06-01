public final class TypeChecker {

    public Type typeOf(Context ctx, Term term) {

        if (term instanceof Var v) {

            Type t = ctx.lookupTerm(v.name());

            if (t == null) {
                throw new RuntimeException(
                        "unbound variable- " + v.name());
            }

            return t;
        }

        if (term instanceof Abs abs) {
            checkWellFormedType(
                    ctx,
                    abs.paramType());

            Context next =
                    ctx.copy();

            next.addTerm(
                    abs.param(),
                    abs.paramType());

            Type bodyType =
                    typeOf(
                            next,
                            abs.body());

            return new FunctionType(
                    abs.paramType(),
                    bodyType);
        }

        if (term instanceof App app) {

            Type functionType =
                    typeOf(ctx, app.function());

            Type argumentType =
                    typeOf(ctx, app.argument());

            if (!(functionType instanceof FunctionType fn)) {
                throw new RuntimeException(
                        "non-function");
            }

            if (!fn.from().equals(argumentType)) {
                throw new RuntimeException(
                        "type mismatch");
            }

            return fn.to();
        }

        if (term instanceof TypeAbs ta) {

            Context next =
                    ctx.copy();

            next.addType(
                    ta.typeVar());

            Type bodyType =
                    typeOf(
                            next,
                            ta.body());

            Type result =
                    new ForAllType(
                            ta.typeVar(),
                            bodyType);

            checkWellFormedType(
                    ctx,
                    result);

            return result;
        }
        if (term instanceof TypeApp ta) {
            checkWellFormedType(
                    ctx,
                    ta.typeArgument());

            Type termType =
                    typeOf(
                            ctx,
                            ta.term());

            if (!(termType
                    instanceof ForAllType fa)) {

                throw new RuntimeException(
                        "expected universal type");
            }

            return TypeSub.substitute(
                    fa.body(),
                    fa.typeVar(),
                    ta.typeArgument());
        }

        throw new IllegalStateException();
    }
    private void checkWellFormedType(
        Context ctx,
        Type type) {

    if (type instanceof TypeVar tv) {

        if (!ctx.containsType(tv.name())) {
            throw new RuntimeException(
                    "unbound type variable- "
                            + tv.name());
        }

        return;
    }

    if (type instanceof FunctionType fn) {

        checkWellFormedType(
                ctx,
                fn.from());

        checkWellFormedType(
                ctx,
                fn.to());

        return;
    }

    if (type instanceof ForAllType fa) {

        Context next =
                ctx.copy();

        next.addType(
                fa.typeVar());

        checkWellFormedType(
                next,
                fa.body());

        return;
    }

    throw new RuntimeException(
            "undefined type");
}
}