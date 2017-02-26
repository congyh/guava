package com.google.common.collect;

/**
 * 这个类是为了测试ImmutableCollection中所说的在这个package之外不能扩展Immutable的类是不是真的
 *
 * 解释: 因为在ImmutableCollection等一众类中将构造方法设置为了protected类型, 所以如果不在相同的包内extend的话,
 * 是调用不到父类的构造方法的, 所以就不能完成类的扩展.
 *
 * @author <a href="mailto:yihao.cong@outlook.com">Cong Yihao</a>
 * @since version: 0.0.1  date: 17-2-24
 */
public abstract class ImmutableInheritTest<E> extends ImmutableCollection<E> {
    public ImmutableInheritTest() {

    }
}
