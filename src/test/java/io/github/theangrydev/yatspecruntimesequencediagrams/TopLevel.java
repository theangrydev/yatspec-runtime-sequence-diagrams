package io.github.theangrydev.yatspecruntimesequencediagrams;

public class TopLevel {

    private final NextLevel nextLevel = new NextLevel();

    public void test() {
        System.out.println("hello");
        two();
    }

    private void two() {
        String asdasd = nextLevel.foo("asdasd");
        System.out.println("asdasd = " + asdasd);
    }
}
