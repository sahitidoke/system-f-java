public class Main {

    static final TypeChecker checker = new TypeChecker();
    static final PrettyTypePrinter typ = new PrettyTypePrinter();
    static final Eval eval = new Eval();
    static final PrettyTermPrinter tep = new PrettyTermPrinter();
    static int passed = 0;
    static int failed = 0;

    static Context emptyCtx() {
        return new Context();
    }

    static Context ctxWithTypes(String... typeVars) {
        Context ctx = new Context();
        for (String tv : typeVars) ctx.addType(tv);
        return ctx;
    }
    static Context ctxWithTerm(String termName, Type termType, String... typeVars) {
        Context ctx = ctxWithTypes(typeVars);
        ctx.addTerm(termName, termType);
        return ctx;
    }
    static void run(String name, Runnable r) {
    System.out.print("  " + name + " ... ");
    try {
        r.run();
        System.out.println("PASS");
        passed++;
    } catch (AssertionError e) {
        System.out.println("FAIL — " + e.getMessage());
        failed++;
    } catch (Exception e) {
        System.out.println("ERROR — " + e.getMessage());
        failed++;
    }
    }
    static void assertEqual(String expected, String actual) {
    System.out.println();
    System.out.println("    expected: " + expected);
    System.out.println("    result:   " + actual);

    if (!expected.equals(actual)) {
        throw new AssertionError("values differ");
    }
    }
    
    static void assertThrows(Runnable r) {
        try {
            r.run();
            throw new AssertionError("expected an exception but none was thrown");
        } catch (AssertionError e) {
            throw e;
        } catch (Exception ignored) { }
    }
    public static void main(String[] args) {
        System.out.println("\n======================================");
        System.out.println("  EVALUATION RULES");
        System.out.println("======================================\n");

        run("E-APPABS | beta reduction ",
    () -> {
        Term t = new App(
                new Abs("x", new TypeVar("X"), new Var("x")),
                new Abs("y", new TypeVar("X"), new Var("y")));
        assertEqual("λy:X.y", tep.print(eval.step(t)));
    });

    run("E-APPABS | substitutes into body",
    () -> {
        Term t = new App(
                new Abs("x", new TypeVar("X"), new App(new Var("x"), new Var("x"))),
                new Abs("y", new TypeVar("X"), new Var("y")));
        assertEqual("(λy:X.y λy:X.y)", tep.print(eval.step(t)));
    });

        run("E-APP1 | reduces function side first",
        () -> {
        Term inner = new App(
                new Abs("x", new TypeVar("X"), new Var("x")),
                new Abs("y", new TypeVar("X"), new Var("y")));
        Term outer = new App(inner, new Abs("z", new TypeVar("X"), new Var("z")));
        assertEqual("(λy:X.y λz:X.z)", tep.print(eval.step(outer)));
        });

        run("E-APP1 | reduces argument side if function is a value  ",
        () -> {
        Term fun = new App(
                new Abs("f", new TypeVar("X"), new Var("f")),
                new Abs("a", new TypeVar("X"), new Var("a")));
        Term arg = new App(
                new Abs("g", new TypeVar("X"), new Var("g")),
                new Abs("b", new TypeVar("X"), new Var("b")));
        Term result = eval.step(new App(fun, arg));
        assertEqual("(λa:X.a (λg:X.g λb:X.b))", tep.print(result));
        });

        run("E-APP2 | reduces argument side if function is a value  ",
        () -> {
        Term fun = new Abs("x", new TypeVar("X"), new Var("x"));
        Term arg = new App(
                new Abs("y", new TypeVar("X"), new Var("y")),
                new Abs("z", new TypeVar("X"), new Var("z")));
        Term result = eval.step(new App(fun, arg));
        assertEqual("(λx:X.x λz:X.z)", tep.print(result));
        });

        run("E-TAPP | reduces term side of  type application first",
        () -> {
        Term inner = new App(
                new Abs("f", new TypeVar("X"), new Var("f")),
                new TypeAbs("X", new Abs("x", new TypeVar("X"), new Var("x"))));
        Term t = new TypeApp(inner, new TypeVar("X"));
        assertEqual("(ΛX.λx:X.x)[X]", tep.print(eval.step(t)));
        }); 

        run("E-TAPPTABS | beta reduction of type application",
        () -> {
        Term t = new TypeApp(
                new TypeAbs("X", new Abs("x", new TypeVar("X"), new Var("x"))),
                new TypeVar("Nat"));
        assertEqual("λx:Nat.x", tep.print(eval.step(t)));
        });

        run("E-TAPPTABS | substitutes into body",
        () -> {
        Term twice = new TypeAbs("X",
                new Abs("f", new FunctionType(new TypeVar("X"), new TypeVar("X")),
                        new Abs("x", new TypeVar("X"),
                        new App(new Var("f"), new App(new Var("f"), new Var("x"))))));
        Term t = new TypeApp(twice, new TypeVar("Nat"));
        assertEqual("λf:Nat→Nat.λx:Nat.(f (f x))", tep.print(eval.step(t)));
        });


        System.out.println("\n======================================");
        System.out.println("  TYPING RULES");
        System.out.println("======================================\n");

        System.out.println("  -- T-VAR --");

        run("T-VAR | testing if variable lookup returns bound type",
            () -> {
                Context ctx = ctxWithTerm("x", new TypeVar("X"), "X");
                assertEqual("X", typ.print(checker.typeOf(ctx, new Var("x"))));
            });

        run("T-VAR | testing if picks correct binding among several",
            () -> {
                Context ctx = ctxWithTypes("A", "B", "C");
                ctx.addTerm("a", new TypeVar("A"));
                ctx.addTerm("b", new TypeVar("B"));
                ctx.addTerm("c", new TypeVar("C"));
                assertEqual("A", typ.print(checker.typeOf(ctx, new Var("a"))));
                assertEqual("B", typ.print(checker.typeOf(ctx, new Var("b"))));
                assertEqual("C", typ.print(checker.typeOf(ctx, new Var("c"))));
            });

        run("T-VAR | testing if unbound variable throws",
            () -> assertThrows(() -> checker.typeOf(emptyCtx(), new Var("z"))));

        run("T-VAR | testing if variable bound to a function type",
            () -> {
                Context ctx = ctxWithTypes("X");
                ctx.addTerm("f", new FunctionType(new TypeVar("X"), new TypeVar("X")));
                assertEqual("X→X", typ.print(checker.typeOf(ctx, new Var("f"))));
            });

        System.out.println("\n  -- T-ABS --");

        run("T-ABS | testing identity  λx:X.x  :  X→X",
            () -> {
                Context ctx = ctxWithTypes("X");
                Term id = new Abs("x", new TypeVar("X"), new Var("x"));
                assertEqual("X→X", typ.print(checker.typeOf(ctx, id)));
            });

        run("T-ABS | testingconstant function  λx:X.λy:Y.x  :  X→Y→X",
            () -> {
                Context ctx = ctxWithTypes("X", "Y");
                Term t = new Abs("x", new TypeVar("X"),
                             new Abs("y", new TypeVar("Y"), new Var("x")));
                assertEqual("X→Y→X", typ.print(checker.typeOf(ctx, t)));
            });

        run("T-ABS |testing higher-order arg  λf:X→X.λx:X.f x  :  (X→X)→X→X",
            () -> {
                Context ctx = ctxWithTypes("X");
                Term t = new Abs("f", new FunctionType(new TypeVar("X"), new TypeVar("X")),
                             new Abs("x", new TypeVar("X"),
                                 new App(new Var("f"), new Var("x"))));
                assertEqual("(X→X)→X→X", typ.print(checker.typeOf(ctx, t)));
            });

        run("T-ABS |testing if unbound type in annotation throws",
            () -> assertThrows(
                    () -> checker.typeOf(emptyCtx(), new Abs("x", new TypeVar("X"), new Var("x")))));

        System.out.println("\n  -- T-APP --");

        run("T-APP | apply identity to abstraction value",
            () -> {
                Context ctx = ctxWithTypes("X");
                Term fun = new Abs("x", new FunctionType(new TypeVar("X"), new TypeVar("X")), new Var("x"));
                Term arg = new Abs("y", new TypeVar("X"), new Var("y"));
                assertEqual("X→X", typ.print(checker.typeOf(ctx, new App(fun, arg))));
            });

        run("T-APP | function composition type",
            () -> {
                Context ctx = ctxWithTypes("X", "Y", "Z");
                Term t = new Abs("f", new FunctionType(new TypeVar("X"), new TypeVar("Y")),
                             new Abs("g", new FunctionType(new TypeVar("Y"), new TypeVar("Z")),
                                 new Abs("x", new TypeVar("X"),
                                     new App(new Var("g"), new App(new Var("f"), new Var("x"))))));
                assertEqual("(X→Y)→(Y→Z)→X→Z", typ.print(checker.typeOf(ctx, t)));
            });

        run("T-APP | argument type mismatch throws",
            () -> {
                Context ctx = ctxWithTypes("X", "Y");
                Term fun = new Abs("x", new TypeVar("X"), new Var("x"));
                Term arg = new Abs("y", new TypeVar("Y"), new Var("y"));
                assertThrows(() -> checker.typeOf(ctx, new App(fun, arg)));
            });

        run("T-APP | applying non-function throws",
            () -> {
                Context ctx = ctxWithTerm("x", new TypeVar("X"), "X");
                assertThrows(() -> checker.typeOf(ctx, new App(new Var("x"), new Var("x"))));
            });

        System.out.println("\n  -- T-TABS --");

        run("T-TABS | polymorphic identity  λX.λx:X.x  :  ∀X.X→X",
            () -> {
                Term t = new TypeAbs("X", new Abs("x", new TypeVar("X"), new Var("x")));
                assertEqual("∀X.X→X", typ.print(checker.typeOf(emptyCtx(), t)));
            });

        run("T-TABS |   λX.λt:X.λf:X.t  :  ∀X.X→X→X",
            () -> {
                Term tru = new TypeAbs("X",
                               new Abs("t", new TypeVar("X"),
                                   new Abs("f", new TypeVar("X"), new Var("t"))));
                assertEqual("∀X.X→X→X", typ.print(checker.typeOf(emptyCtx(), tru)));
            });

        run("T-TABS |  λX.λf:X→X.λx:X.f(f x)  :  ∀X.(X→X)→X→X",
            () -> {
                Term twice = new TypeAbs("X",
                    new Abs("f", new FunctionType(new TypeVar("X"), new TypeVar("X")),
                        new Abs("x", new TypeVar("X"),
                            new App(new Var("f"), new App(new Var("f"), new Var("x"))))));
                assertEqual("∀X.(X→X)→X→X", typ.print(checker.typeOf(emptyCtx(), twice)));
            });

        run("T-TABS | nested  λX.λY.λf:X→Y.λx:X.f x  :  ∀X.∀Y.(X→Y)→X→Y",
            () -> {
                Term t = new TypeAbs("X",
                    new TypeAbs("Y",
                        new Abs("f", new FunctionType(new TypeVar("X"), new TypeVar("Y")),
                            new Abs("x", new TypeVar("X"),
                                new App(new Var("f"), new Var("x"))))));
                assertEqual("∀X.∀Y.(X→Y)→X→Y", typ.print(checker.typeOf(emptyCtx(), t)));
            });

        System.out.println("\n  -- T-TAPP --");

        run("T-TAPP | identity instantiated at Nat  :  Nat→Nat",
            () -> {
                Context ctx = ctxWithTypes("Nat");
                Term polyId = new TypeAbs("X", new Abs("x", new TypeVar("X"), new Var("x")));
                assertEqual("Nat→Nat", typ.print(checker.typeOf(ctx, new TypeApp(polyId, new TypeVar("Nat")))));
            });

        run("T-TAPP | twice instantiated at Nat  :  (Nat→Nat)→Nat→Nat",
            () -> {
                Context ctx = ctxWithTypes("Nat");
                Term twice = new TypeAbs("X",
                    new Abs("f", new FunctionType(new TypeVar("X"), new TypeVar("X")),
                        new Abs("x", new TypeVar("X"),
                            new App(new Var("f"), new App(new Var("f"), new Var("x"))))));
                assertEqual("(Nat→Nat)→Nat→Nat",
                        typ.print(checker.typeOf(ctx, new TypeApp(twice, new TypeVar("Nat")))));
            });

        run("T-TAPP | instantiated at a function type  :  (Nat→Nat)→Nat→Nat",
            () -> {
                Context ctx = ctxWithTypes("Nat");
                Term polyId = new TypeAbs("X", new Abs("x", new TypeVar("X"), new Var("x")));
                Type natToNat = new FunctionType(new TypeVar("Nat"), new TypeVar("Nat"));
                assertEqual("(Nat→Nat)→Nat→Nat",
                        typ.print(checker.typeOf(ctx, new TypeApp(polyId, natToNat))));
            });

        run("T-TAPP | chained instantiation ∀X.∀Y at Nat then Bool",
            () -> {
                Context ctx = ctxWithTypes("Nat", "Bool");
                Term t = new TypeAbs("X",
                    new TypeAbs("Y",
                        new Abs("f", new FunctionType(new TypeVar("X"), new TypeVar("Y")),
                            new Abs("x", new TypeVar("X"),
                                new App(new Var("f"), new Var("x"))))));
                Type result = checker.typeOf(ctx,
                        new TypeApp(new TypeApp(t, new TypeVar("Nat")), new TypeVar("Bool")));
                assertEqual("(Nat→Bool)→Nat→Bool", typ.print(result));
            });

        run("T-TAPP | applying non-universal type throws",
            () -> {
                Context ctx = ctxWithTypes("X", "Nat");
                Term notPoly = new Abs("x", new TypeVar("X"), new Var("x"));
                assertThrows(() -> checker.typeOf(ctx, new TypeApp(notPoly, new TypeVar("Nat"))));
            });

        run("T-TAPP | unbound type argument throws",
            () -> {
                Term polyId = new TypeAbs("X", new Abs("x", new TypeVar("X"), new Var("x")));
                assertThrows(() -> checker.typeOf(emptyCtx(), new TypeApp(polyId, new TypeVar("Nat"))));
            });

        System.out.printf("%n======================================%n");
        System.out.printf("  %d passed  |  %d failed%n", passed, failed);
        System.out.printf("======================================%n");
    }
}
