package com.zetkoofficial.cmdargs.object;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation exposes the static field to the ArgumentBuilder. <br>
 * It should be used on objects which have a constructor of type {@link java.lang.String String}. It also supports arrays of all these objects.
 * @author ZetkoOfficial
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Argument {
	String[] verboseTags();
	String[] shortTags();
	String info();
}
