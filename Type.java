public sealed interface Type
        permits TypeVar, FunctionType, ForAllType {
}
 record TypeVar(String name) implements Type {
}
 record FunctionType(
        Type from,
        Type to
) implements Type {
}
 record ForAllType(
        String typeVar,
        Type body
) implements Type {
}
