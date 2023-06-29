/*
	This code is from Skedule 0.4.0 (https://github.com/shyiko/skedule/tree/0.4.0),
	which is licensed under the MIT license (https://opensource.org/license/mit).

	The reason this code is not a 'proper' dependency is because it's compiled for
	an old version of Kotlin, and causes build errors when used with current versions
	of Kotlin.
*/
package moe.absolucy.backup.common.schedule;

/**
 * Thrown in case of invalid schedule (during parsing, programmatic construction, etc).
 */
public class InvalidScheduleException extends RuntimeException {
	public InvalidScheduleException(String message) {
		super(message);
	}
}