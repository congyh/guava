/*
 * Copyright (C) 2010 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.common.base;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.VisibleForTesting;
import javax.annotation.Nullable;

/**
 * Static utility methods pertaining(适用于) to {@code String} or {@code CharSequence} instances.
 *
 * @author Kevin Bourrillion
 * @since 3.0
 */
@GwtCompatible
public final class Strings {
  private Strings() {}

  /**
   * Returns the given string if it is non-null; the empty string otherwise.
   *
   * @param string the string to test and possibly return
   * @return {@code string} itself if it is non-null; {@code ""} if it is null
   */
  public static String nullToEmpty(@Nullable String string) {
    return (string == null) ? "" : string;
  }

  /**
   * Returns the given string if it is nonempty; {@code null} otherwise.
   *
   * @param string the string to test and possibly return
   * @return {@code string} itself if it is nonempty; {@code null} if it is empty or null
   */
  @Nullable
  public static String emptyToNull(@Nullable String string) {
    return isNullOrEmpty(string) ? null : string;
  }

  /**
   * Returns {@code true} if the given string is null or is the empty string.
   *
   * <p>Consider normalizing your string references with {@link #nullToEmpty}. If you do, you can
   * use {@link String#isEmpty()} instead of this method, and you won't need special null-safe forms
   * of methods like {@link String#toUpperCase} either. Or, if you'd like to normalize "in the other
   * direction," converting empty strings to {@code null}, you can use {@link #emptyToNull}.
   *
   * @param string a string reference to check
   * @return {@code true} if the string is null or is the empty string
   *
   * 实际上{@link Platform#stringIsNullOrEmpty(String)} 底层就是通过简单的null和isEmpty来判断的
   */
  public static boolean isNullOrEmpty(@Nullable String string) {
    return Platform.stringIsNullOrEmpty(string);
  }

  /**
   * 作用是将一个字符串补全到一定的字节数, 如果字符串本身的长度超出了指定的字节数, 那么直接补全
   *
   * Returns a string, of length at least {@code minLength}, consisting of {@code string} prepended
   * with as many copies of {@code padChar} as are necessary to reach that length. For example,
   *
   * <ul>
   * <li>{@code padStart("7", 3, '0')} returns {@code "007"}
   * <li>{@code padStart("2010", 3, '0')} returns {@code "2010"}
   * </ul>
   *
   * <p>See {@link java.util.Formatter} for a richer set of formatting capabilities.
   *
   * @param string the string which should appear at the end of the result
   * @param minLength the minimum length the resulting string must have. Can be zero or negative, in
   *     which case the input string is always returned.
   * @param padChar the character to insert at the beginning of the result until the minimum length
   *     is reached
   * @return the padded string
   */
  public static String padStart(String string, int minLength, char padChar) {
    checkNotNull(string); // eager for GWT.
    if (string.length() >= minLength) {
      return string;
    }
    StringBuilder sb = new StringBuilder(minLength);
    for (int i = string.length(); i < minLength; i++) {
      sb.append(padChar);
    }
    sb.append(string);
    return sb.toString();
  }

  /**
   * Returns a string, of length at least {@code minLength}, consisting of {@code string} appended
   * with as many copies of {@code padChar} as are necessary to reach that length. For example,
   *
   * <ul>
   * <li>{@code padEnd("4.", 5, '0')} returns {@code "4.000"}
   * <li>{@code padEnd("2010", 3, '!')} returns {@code "2010"}
   * </ul>
   *
   * <p>See {@link java.util.Formatter} for a richer set of formatting capabilities.
   *
   * @param string the string which should appear at the beginning of the result
   * @param minLength the minimum length the resulting string must have. Can be zero or negative, in
   *     which case the input string is always returned.
   * @param padChar the character to append to the end of the result until the minimum length is
   *     reached
   * @return the padded string
   */
  public static String padEnd(String string, int minLength, char padChar) {
    checkNotNull(string); // eager for GWT.
    if (string.length() >= minLength) {
      return string;
    }
    StringBuilder sb = new StringBuilder(minLength);
    sb.append(string);
    for (int i = string.length(); i < minLength; i++) {
      sb.append(padChar);
    }
    return sb.toString();
  }

  /**
   * Returns a string consisting of a specific number of concatenated copies of an input string. For
   * example, {@code repeat("hey", 3)} returns the string {@code "heyheyhey"}.
   *
   * @param string any non-null string
   * @param count the number of times to repeat it; a nonnegative integer
   * @return a string containing {@code string} repeated {@code count} times (the empty string if
   *     {@code count} is zero)
   * @throws IllegalArgumentException if {@code count} is negative
   */
  public static String repeat(String string, int count) {
    checkNotNull(string); // eager for GWT.

    if (count <= 1) {
      checkArgument(count >= 0, "invalid count: %s", count);
      return (count == 0) ? "" : string;
    }

    // IF YOU MODIFY THE CODE HERE, you must update StringsRepeatBenchmark
    final int len = string.length();
    // 这里目的是看int是否会溢出, 如果溢出了抛出异常
    final long longSize = (long) len * (long) count;
    final int size = (int) longSize;
    if (size != longSize) {
      throw new ArrayIndexOutOfBoundsException("Required array size too large: " + longSize);
    }

    final char[] array = new char[size];
    string.getChars(0, len, array, 0);
    int n;

    /**
     * 这里的n是按位左移, 举个例子, 假设现在我们的string是"abc", 然后count是5
     * 所以size变成了15, 那么最初的n的二进制表示形式是11, 小于15-3,
     * 循环一次n变为了110, 也就是6, 小于15-6
     * 再循环一次n变为了1100, 也就是12, 大于15-12循环停止
     * 然后出了循环把每复制完的补齐即可
     *
     * [小结]
     * 1. 这里为什么要使用<<操作?
     *  因为按位左移操作每进行一次, 相当于*2的操作, 数组的这种copy方法最快速
     * 2. System.arraycopy()是native的方法, 效率首选
     */
    for (n = len; n < size - n; n <<= 1) {
      System.arraycopy(array, 0, array, n, n);
    }
    System.arraycopy(array, 0, array, n, size - n);
    // String类型可以采用一个字符数组来构造
    return new String(array);
  }

  /**
   * Returns the longest string {@code prefix} such that
   * {@code a.toString().startsWith(prefix) && b.toString().startsWith(prefix)}, taking care not to
   * split surrogate pairs. If {@code a} and {@code b} have no common prefix, returns the empty
   * string.
   *
   * 这里说的是注意不要匹配了一个一半的UTF-16编码的字符, 也就是说你匹配长度的最大值可能是UTF-16编码的高半代理,
   * 需要手动去掉
   *
   * @since 11.0
   */
  public static String commonPrefix(CharSequence a, CharSequence b) {
    checkNotNull(a);
    checkNotNull(b);

    int maxPrefixLength = Math.min(a.length(), b.length());
    int p = 0;
    while (p < maxPrefixLength && a.charAt(p) == b.charAt(p)) {
      p++;
    }
    // 只要两者有一个是高半代理, 就应该将此位去掉
    if (validSurrogatePairAt(a, p - 1) || validSurrogatePairAt(b, p - 1)) {
      p--;
    }
    return a.subSequence(0, p).toString();
  }

  /**
   * Returns the longest string {@code suffix} such that
   * {@code a.toString().endsWith(suffix) && b.toString().endsWith(suffix)}, taking care not to
   * split surrogate pairs. If {@code a} and {@code b} have no common suffix, returns the empty
   * string.
   *
   * 可以参见上面的{@link #commonPrefix(CharSequence, CharSequence)}的解释
   *
   * @since 11.0
   */
  public static String commonSuffix(CharSequence a, CharSequence b) {
    checkNotNull(a);
    checkNotNull(b);

    int maxSuffixLength = Math.min(a.length(), b.length());
    int s = 0;
    while (s < maxSuffixLength && a.charAt(a.length() - s - 1) == b.charAt(b.length() - s - 1)) {
      s++;
    }
    if (validSurrogatePairAt(a, a.length() - s - 1)
        || validSurrogatePairAt(b, b.length() - s - 1)) {
      s--;
    }
    return a.subSequence(a.length() - s, a.length()).toString();
  }

  /**
   * True when a valid surrogate pair starts at the given {@code index} in the given {@code string}.
   * Out-of-range indexes return false.
   *
   * 检查是否是一个UTF-16编码的字符
   */
  @VisibleForTesting
  static boolean validSurrogatePairAt(CharSequence string, int index) {
    return index >= 0
        && index <= (string.length() - 2)
        && Character.isHighSurrogate(string.charAt(index))
        && Character.isLowSurrogate(string.charAt(index + 1));
  }
}
