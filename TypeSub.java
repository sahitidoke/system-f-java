public final class TypeSub {

    private TypeSub() {
    }

    public static Type substitute(
            Type type,
            String variable,
            Type replacement) {

        if (type instanceof TypeVar tv) {
            return tv.name().equals(variable)
                    ? replacement
                    : tv;
        }

        if (type instanceof FunctionType fn) {
            return new FunctionType(
                    substitute(fn.from(), variable, replacement),
                    substitute(fn.to(), variable, replacement)
            );
        }

        if (type instanceof ForAllType fa) {

            if (fa.typeVar().equals(variable)) {
                return fa;
            }

            return new ForAllType(
                    fa.typeVar(),
                    substitute(
                            fa.body(),
                            variable,
                            replacement)
            );
        }

        throw new IllegalStateException();
    }
}