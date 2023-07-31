package trik.testsys.webclient.utils.fp

/**
 * @author Viktor Karasev
 * @since 1.1.0
 */
class Either<L, R> private constructor(
    private var left: L?,
    private var right: R?
) {

    fun isLeft(): Boolean = left != null

    fun isRight(): Boolean = right != null

    fun getLeft(): L = left!!

    fun getRight(): R = right!!

    fun <R2>bind(f: (R) -> Either<L, R2>): Either<L, R2> =
        if (isLeft()) left(getLeft()) else f(getRight())

    companion object {
        fun <L, R> left(left: L): Either<L, R> = Either(left, null)

        fun <L, R> right(right: R): Either<L, R> = Either(null, right)
    }
}