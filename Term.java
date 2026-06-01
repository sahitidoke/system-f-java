public sealed interface Term
        permits Var, Abs, App, TypeAbs, TypeApp {
}
 record Var(String name) implements Term {
}
 record Abs(
        String param,
        Type paramType,
        Term body
) implements Term {
}
 record App(
        Term function,
        Term argument
) implements Term {
}
 record TypeAbs(
        String typeVar,
        Term body
) implements Term {
}
 record TypeApp(
        Term term,
        Type typeArgument
) implements Term {
}

