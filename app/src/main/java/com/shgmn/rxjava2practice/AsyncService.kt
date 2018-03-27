package com.shgmn.rxjava2practice

import io.reactivex.*
import java.util.concurrent.TimeUnit


/**
 * Created by tshigemo on 2018/03/27.
 */
class AsyncService {

    /**
     * Single
     *
     * 1つの値を流すStream.
     */
    fun callSingle(user: User, delay: Long): Single<User> {
        return Single.just(user).delay(delay, TimeUnit.SECONDS)
    }

    /**
     * Single
     *
     * 1つの値を流すStream.
     */
    fun callSingleError(): Single<User> {
        return Single.error<User>(IllegalArgumentException("ERROR")).delay(5, TimeUnit.SECONDS)
    }

    /**
     * Completable
     *
     * 値を流さないStream.
     */
    fun callCompletable(): Completable {
        return Completable.complete().delay(5, TimeUnit.SECONDS)
    }

    /**
     * Completable
     *
     * 値を流さないStream.
     */
    fun callCompletableError(): Completable {
        return Completable.error(IllegalArgumentException("ERROR")).delay(5, TimeUnit.SECONDS)
    }

}