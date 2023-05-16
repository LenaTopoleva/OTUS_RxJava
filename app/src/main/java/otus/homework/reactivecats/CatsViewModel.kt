package otus.homework.reactivecats

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import io.reactivex.Observable.interval
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CatsViewModel(
    private val catsService: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator
) : ViewModel() {

    private val _catsLiveData = MutableLiveData<Result>()
    val catsLiveData: LiveData<Result> = _catsLiveData

    var compositeDisposable = CompositeDisposable()

    init {
        compositeDisposable.add(
            getFactsPeriodically()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.text.isNotEmpty()) {
                            _catsLiveData.value = Success(it)
                        } else {
                            _catsLiveData.value = Error(null)
                        }
                    },
                    { _catsLiveData.value = Error(it.message) }
                ))
    }

    private fun getCatFact() {
        compositeDisposable.add(
            catsService.getCatFact()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it.text.isNotEmpty()) {
                            _catsLiveData.value = Success(it)
                        } else {
                            _catsLiveData.value = Error(null)
                        }
                    },
                    { _catsLiveData.value = Error(it.message) }
                ))
    }

    /**
     * Реализуйте функцию otus.homework.reactivecats.CatsViewModel#getFacts следующим образом:
     * каждые 2 секунды идем в сеть за новым фактом, если сетевой запрос завершился неуспешно, то в
     * качестве фоллбека идем за фактом в уже реализованный otus.homework.reactivecats.LocalCatFactsGenerator#generateCatFact.
     */
    @SuppressLint("CheckResult")
    private fun getFactsPeriodically() =
        interval(0, 2000, TimeUnit.MILLISECONDS)
            .flatMap { catsService.getCatFact() }
            .onErrorResumeNext(localCatFactsGenerator.generateCatFact())
            .subscribeOn(Schedulers.io())

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}

class CatsViewModelFactory(
    private val catsRepository: CatsService,
    private val localCatFactsGenerator: LocalCatFactsGenerator
) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        CatsViewModel(catsRepository, localCatFactsGenerator) as T
}

sealed class Result
data class Success(val fact: Fact) : Result()
data class Error(val message: String?) : Result()