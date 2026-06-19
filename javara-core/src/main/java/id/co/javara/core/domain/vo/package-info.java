/**
 * Domain value objects (Temenos Transact).
 *
 * <p>Immutable records representing core domain primitives:
 * <ul>
 *   <li>{@link AccountNumber} — T24 account identifier</li>
 *   <li>{@link CustomerId} — customer identifier</li>
 *   <li>{@link T24Reference} — T24 transaction reference</li>
 *   <li>{@link Amount} — monetary value with currency</li>
 *   <li>{@link IdempotencyKey} — idempotency key for safe retries</li>
 * </ul>
 */
package id.co.javara.core.domain.vo;
