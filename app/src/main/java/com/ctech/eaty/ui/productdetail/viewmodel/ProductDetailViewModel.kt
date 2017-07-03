package com.ctech.eaty.ui.productdetail.viewmodel

import android.support.customtabs.CustomTabsSession
import com.ctech.eaty.entity.ProductDetail
import com.ctech.eaty.ui.productdetail.navigation.ProductDetailNavigation
import com.ctech.eaty.ui.productdetail.state.ProductDetailState
import com.ctech.eaty.util.ResizeImageUrlProvider
import com.ctech.eaty.util.rx.ThreadScheduler
import io.reactivex.Observable
import io.reactivex.internal.functions.Functions
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject


class ProductDetailViewModel(private val stateDispatcher: BehaviorSubject<ProductDetailState>,
                             private val navigation: ProductDetailNavigation,
                             private val threadScheduler: ThreadScheduler) {
    private val HEADER_IMAGE_SIZE = 300
    private val MAX_BODY_ITEM = 7
    private var body: List<ProductBodyItemViewModel> = emptyList()
    private val bodySubject: PublishSubject<List<ProductBodyItemViewModel>> = PublishSubject.create()

    fun loading(): Observable<ProductDetailState> {
        return stateDispatcher
                .observeOn(threadScheduler.uiThread())
                .filter { it.loading && it.content == ProductDetail.EMPTY }
    }


    fun loadError(): Observable<Throwable> {
        return stateDispatcher
                .observeOn(threadScheduler.uiThread())
                .filter { it.error != null && !it.loading }
                .map { it.error!! }

    }

    fun header(): Observable<String> {
        return content()
                .map {
                    ResizeImageUrlProvider.overrideUrl(it.thumbnail.imageUrl, HEADER_IMAGE_SIZE)
                }
    }

    fun content(): Observable<ProductDetail> {
        return stateDispatcher
                .observeOn(threadScheduler.uiThread())
                .filter {
                    !it.loading && it.error == null
                }
                .flatMap {
                    if (it.content == null)
                        Observable.just(ProductDetail.EMPTY)
                    else
                        Observable.just(it.content)
                }

    }

    fun body(): Observable<List<ProductBodyItemViewModel>> {
        return content()
                .filter {
                    it != ProductDetail.EMPTY
                }
                .map {
                    val body = ArrayList<ProductBodyItemViewModel>(MAX_BODY_ITEM)
                    body += mapHeader(it)
                    if (it.comments.size > 5) {
                        body += it.comments.take(5).map {
                            CommentItemViewModel(it)
                        }
                    } else {
                        body += it.comments.map {
                            CommentItemViewModel(it)
                        }
                    }
                    body += mapRecommend(it)
                    this.body = body
                    body
                }

    }


    private fun mapRecommend(productDetail: ProductDetail): ProductRecommendItemViewModel = ProductRecommendItemViewModel(productDetail.relatedPosts)

    private fun mapHeader(productDetail: ProductDetail): ProductHeaderItemViewModel = ProductHeaderItemViewModel(productDetail)

    fun commentsSelection(): Observable<List<ProductBodyItemViewModel>> {
        return bodySubject
                .subscribeOn(threadScheduler.workerThread())
                .observeOn(threadScheduler.uiThread())
    }

    fun selectCommentAt(position: Int) {

        body = body.mapIndexed { index, itemViewModel ->
            if (itemViewModel is CommentItemViewModel) {
                val viewModel = itemViewModel
                if (index == position) {
                    viewModel.copy(selected = !viewModel.isSelected)
                } else {
                    viewModel.copy(selected = false)
                }
            } else {
                return@mapIndexed itemViewModel
            }
        }
        bodySubject.onNext(body)
    }

    fun navigateToVote() {
        val product = stateDispatcher.value.content
        product?.run {
            navigation.toVote(id, voteCount).subscribe()
        }
    }

    fun navigateComment() {
        val product = stateDispatcher.value.content
        product?.run {
            navigation.toComment(id).subscribe()
        }
    }

    fun shareLink() {
        val product = stateDispatcher.value.content
        product?.run {
            navigation.toShare(redirectUrl).subscribe()
        }
    }

    fun getProduct(session: CustomTabsSession) {
        val product = stateDispatcher.value.content
        product?.run {
            navigation.toUrl(redirectUrl, session).subscribe(Functions.EMPTY_ACTION, Functions.ERROR_CONSUMER)
        }

    }
}