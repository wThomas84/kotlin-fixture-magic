package net.datenstrudel.fixtmagic

class SimpleClass

class SimpleClassWithComplexParam(val paramA: SimpleClass, val paramB: SimpleClass){
    private constructor(paramA: SimpleClass): this(paramA, SimpleClass())
}

