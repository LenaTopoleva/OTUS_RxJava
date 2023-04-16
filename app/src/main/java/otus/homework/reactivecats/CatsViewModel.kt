package otus.homework.reactivecats

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.SingleSource
import io.reactivex.SingleTransformer
import io.reactivex.BackpressureStrategy
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    var compositeDisposable = CompositeDisposable()

    init {
        compositeDisposable.add(getFacts(context)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { _catsLiveData.value = it },
                { _catsLiveData.value = ServerError }
            ))
    }

    private fun getCatFact(context: Context) {
        compositeDisposable.add(catsService.getCatFact()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .compose(ResponseToResultTransformer(context))
            .subscribe(
                { _catsLiveData.value = it },
                { _catsLiveData.value = ServerError }
            ))
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.CatsViewModel#getFacts следующим образом:
     * каждые 2 секунды идем в сеть за новым фактом, если сетевой запрос завершился неуспешно, то в
     * качестве фоллбека идем за фактом в уже реализованный otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact.
     */
    @SuppressLint("CheckResult")
    private fun getFacts(context: Context) =
        Flowable.create({ emitter ->
            catsService.getCatFact()
                .subscribeOn(Schedulers.io())
                .compose(ResponseToResultTransformer(context))
                .onErrorResumeNext(localCatFactsGenerator.generateCatFact().map { Success(it) })
                .repeatUntil { emitter.isCancelled }
                .subscribe({
                    emitter.onNext(it)
                    Thread.sleep(2000)
                }, {
                    emitter.onNext(ServerError)
                    Thread.sleep(2000)
                })
        }, BackpressureStrategy.DROP)

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}

class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator,
    private val context: Context
) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator, context) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String) : Result()
object ServerError : Result()

class ResponseToResultTransformer(
    private val context: Context
) : SingleTransformer<Response<Fact>, Result> {

    override fun apply(upstream: Single<Response<Fact>>): SingleSource<Result> {
        return upstream
            .map { response ->
                return@map if (response.isSuccessful && (response.body() != null)) {
                    Success(response.body()!!)
                } else {
                    Error(
                        response.errorBody()?.string() ?: context.getString(
                            R.string.default_error_text
                        )
                    )
                }
            }
    }
}