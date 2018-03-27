package com.shgmn.rxjava2practice

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.functions.BiFunction
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import org.reactivestreams.Subscription

class MainActivity : AppCompatActivity() {

    val alice = User(1, "Alice")
    val bob = User(2, "Bob")
    val textView: TextView by lazy { findViewById<TextView>(R.id.text) }
    var disposable = Disposables.disposed()
    var subscription: Subscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        case2_5()
    }

    /**
     * subscribeでsuccessとerrorのConsumerを使うパターン.
     */
    fun case1_1() {
        AsyncService().callSingle(alice, 5)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { disposable = it }
                .doFinally { disposable.dispose() }
                .subscribe({ textView.setText(it.name) }, { textView.setText(it.message) })
    }

    /**
     * subscribeでSingleObserverを使うパターン.
     * SingleObserverのoverrideメソッドでdisposableを手に入れられるので、事前にdoOnSubscribe()する必要はない.
     */
    fun case1_2() {
        AsyncService().callSingle(alice, 5)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally { disposable.dispose() }
                .subscribe(object : SingleObserver<User> {
            override fun onSubscribe(d: Disposable) {
                disposable = d
            }

            override fun onSuccess(user: User) {
                textView.setText(user.name)
            }

            override fun onError(e: Throwable) {
                textView.setText(e.message)
            }
        })
    }

    /**
     * subscribeWithでDisposableSingleObserverを使うパターン.
     * 戻り値としてdisposableを手に入れられる.
     */
    fun case1_3() {
        disposable = AsyncService().callSingle(alice, 5)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally { disposable.dispose() }
                .subscribeWith(object : DisposableSingleObserver<User>() {
            override fun onSuccess(user: User) {
                textView.setText(user.name)
            }

            override fun onError(e: Throwable) {
                textView.setText(e.message)
            }
        })
    }

    /**
     * 2つのAPIを待ち合わせるパターン.
     * zip
     */
    fun case2_1() {
        val api1 = AsyncService().callSingle(alice, 5)
        val api2 = AsyncService().callSingle(bob, 5)
        Single.zip(api1, api2, BiFunction<User, User, Pair<User, User>> {
            s1, s2 -> Pair(s1, s2)
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { disposable = it }
                .doFinally { disposable.dispose() }
                .subscribe({ textView.setText(it.first.name + it.second.name) }, { textView.setText(it.message) })
    }

    /**
     * 2つのAPIを直列実行するパターン.
     * concat
     * Single.concat()ではFlowableが得られるのでsubscriptionを使う.
     */
    fun case2_2() {
        val api1 = AsyncService().callSingle(alice, 5)
        val api2 = AsyncService().callSingle(bob, 5)
        Single.concat(api1, api2)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { subscription = it }
                .doFinally { subscription?.cancel() }
                .subscribe(
                        {
                            textView.setText(it.name)
                            subscription?.request(1)
                        }, {
                    textView.setText(it.message)
                })
    }

    /**
     * 2つのAPIを並列実行するパターン.
     * merge
     * Single.merge()ではFlowableが得られるのでsubscriptionを使う.
     */
    fun case2_3() {
        val api1 = AsyncService().callSingle(alice, 3)
        val api2 = AsyncService().callSingle(bob, 5)
        Single.merge(api1, api2)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { subscription = it }
                .doFinally { subscription?.cancel() }
                .subscribe(
                        {
                            textView.setText(it.name)
                            subscription?.request(1)
                        }, {
                    textView.setText(it.message)
                })
    }

    /**
     * 2つのAPIを直列実行し、1つ目のAPIのレスポンス時に2つ目のAPIが遅延して作られるパターン.
     * concat + defer
     * Single.concat()ではFlowableが得られるのでsubscriptionを使う.
     */
    fun case2_4() {
        val api1 = AsyncService().callSingle(alice, 5)
        Single.concat(api1, Single.defer { AsyncService().callSingle(bob, 5) })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { subscription = it }
                .doFinally { subscription?.cancel() }
                .subscribe(
                        {
                            textView.setText(it.name)
                            subscription?.request(1)
                        }, {
                    textView.setText(it.message)
                })
    }

    /**
     * 2つのAPIを直列実行し、1つ目のAPIのレスポンスが2つ目のAPIの入力になるパターン.
     * flatMap
     */
    fun case2_5() {
        AsyncService().callSingle(alice, 5)
                .flatMap { AsyncService().callSingle(User(it.id + bob.id, it.name + bob.name), 5) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { disposable = it }
                .doFinally { disposable.dispose() }
                .subscribe(
                        {
                            textView.setText(it.name)
                        }, {
                    textView.setText(it.message)
                })
    }

}
