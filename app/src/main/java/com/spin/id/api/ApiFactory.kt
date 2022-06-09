package com.spin.id.api

import com.spin.id.api.request.banner.BannerRequest
import com.spin.id.api.request.feed.*
import com.spin.id.api.request.impression.ImpressionRequest
import com.spin.id.api.request.login.AddNumberRequest
import com.spin.id.api.request.login.LoginRequest
import com.spin.id.api.request.login.LoginSSORequest
import com.spin.id.api.request.mission.MissionRequest
import com.spin.id.api.request.order.DetailOrderRequest
import com.spin.id.api.request.order.OrderRequest
import com.spin.id.api.request.otp.OtpRequest
import com.spin.id.api.request.otp.OtpVerifyRequest
import com.spin.id.api.request.preference.UpdatePreferenceRequest
import com.spin.id.api.request.profile.EditUserNameRequest
import com.spin.id.api.request.redeem.RedeemRequest
import com.spin.id.api.request.register.RegisterRequest
import com.spin.id.api.request.register.RegisterSSORequest
import com.spin.id.api.request.resetPassword.ResetRequest
import com.spin.id.api.request.shop.CategoryRequest
import com.spin.id.api.request.shop.ProductCategoryRequest
import com.spin.id.api.request.shop.ProductMasterRequest
import com.spin.id.api.request.shop.buy.PaymentRequest
import com.spin.id.api.request.shop.detail.DetailProductRequest
import com.spin.id.api.request.shop.discount.DiscountRequest
import com.spin.id.api.request.validation.CheckUserRequest
import com.spin.id.api.response.banner.BannerResponse
import com.spin.id.api.response.feed.comment.ChildCommentResponse
import com.spin.id.api.response.feed.comment.CommentResponse
import com.spin.id.api.response.feed.detail.DetailFeedResponse
import com.spin.id.api.response.feed.like.LikeCommentResponse
import com.spin.id.api.response.feed.like.LikeFeedResponse
import com.spin.id.api.response.feed.list.FeedResponse
import com.spin.id.api.response.game.GameResponse
import com.spin.id.api.response.impression.ImpressionResponse
import com.spin.id.api.response.login.LoginResponse
import com.spin.id.api.response.mission.leaderboard.LeaderboardResponse
import com.spin.id.api.response.mission.mission.MissionResponse
import com.spin.id.api.response.mission.redeem.RedeemResponse
import com.spin.id.api.response.order.orderdetail.OrderDetailResponse
import com.spin.id.api.response.order.orderlist.OrderResponse
import com.spin.id.api.response.otp.OtpRequestResponse
import com.spin.id.api.response.otp.OtpSuccessResponse
import com.spin.id.api.response.preference.PreferenceResponse
import com.spin.id.api.response.profile.EditUserNameResponse
import com.spin.id.api.response.register.RegisterResponse
import com.spin.id.api.response.resetPassword.ResetResponse
import com.spin.id.api.response.shop.CategoryResponse
import com.spin.id.api.response.shop.ProductCategoryResponse
import com.spin.id.api.response.shop.ProductMasterResponse
import com.spin.id.api.response.shop.buy.BuyResponse
import com.spin.id.api.response.shop.detail.DetailProductResponse
import com.spin.id.api.response.shop.discount.DiscountResponse
import com.spin.id.api.response.shop.payment.PaymentMethodResponse
import com.spin.id.api.response.topic.TopicResponse
import com.spin.id.api.response.validation.CheckUserResponse
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST


interface ApiFactory {

    /*GET*/

    @GET("get-all-game-category")
    fun getListGame(): Observable<GameResponse>

    @GET("get-all-topic-category")
    fun getListTopic(): Observable<TopicResponse>

    @GET("gamification/leaderboard")
    fun getListLeaderboard(): Observable<LeaderboardResponse>

    /*POST*/

    @POST("users/check")
    fun checkUser(@Body request: CheckUserRequest): Observable<CheckUserResponse>

    @POST("users/login")
    fun login(@Body request: LoginRequest): Observable<LoginResponse>

    @POST("users/sso/login")
    fun login(@Body request: LoginSSORequest): Observable<LoginResponse>

    @POST("users/register")
    fun register(@Body request: RegisterRequest): Observable<RegisterResponse>

    @POST("users/sso/register")
    fun register(@Body request: RegisterSSORequest): Observable<RegisterResponse>

    @POST("users/mobile/update")
    fun addNumber(@Body request: AddNumberRequest): Observable<LoginResponse>

    @POST("users/reset-password")
    fun resetPassword(@Body request: ResetRequest): Observable<ResetResponse>

    @POST("otp/request")
    fun requestOtp(@Body request: OtpRequest): Observable<OtpRequestResponse>

    @POST("otp/verify")
    fun verifyOtp(@Body request: OtpVerifyRequest): Observable<OtpSuccessResponse>

    @POST("users/preferences/set")
    fun updatePreference(@Header("Authorization") token: String, @Body request: UpdatePreferenceRequest): Observable<PreferenceResponse>

    @POST("posts/trending")
    fun feedList(@Body request: FeedRequest): Observable<FeedResponse>

    @POST("posts/like")
    fun likeFeed(@Header("Authorization") token: String, @Body request: LikeFeedRequest): Observable<LikeFeedResponse>

    @POST("comments/post")
    fun commentFeed(@Header("Authorization") token: String, @Body request: CommentFeedRequest): Observable<CommentResponse>

    @POST("comments/comment")
    fun commentOnComment(@Header("Authorization") token: String, @Body request: CommentOnCommentRequest): Observable<ChildCommentResponse>

    @POST("comments/delete")
    fun deleteCommentFeed(@Header("Authorization") token: String, @Body request: DeleteCommentFeedRequest): Observable<CommentResponse>

    @POST("comments/list")
    fun getListComment(@Body request: CommentListFeedRequest): Observable<CommentResponse>

    @POST("comments/child")
    fun getListChildComment(@Body request: ChildCommentFeedRequest): Observable<ChildCommentResponse>

    @POST("comments/like")
    fun likeComment(@Header("Authorization") token: String, @Body request: LikeCommentRequest): Observable<LikeCommentResponse>

    @POST("banner/list")
    fun getListBanner(@Body request: BannerRequest): Observable<BannerResponse>

    @POST("posts/detail")
    fun getDetailFeed(@Body request: DetailFeedRequest): Observable<DetailFeedResponse>

    @POST("profile/edit")
    fun editProfile(
        @Header("Authorization") token: String,
        @Body request: EditUserNameRequest
    ): Observable<EditUserNameResponse>

    @POST("gamification/daily")
    fun getMissionGames(@Body request: MissionRequest): Observable<MissionResponse>

    @POST("gamification/redeem")
    fun redeem(@Body request: RedeemRequest): Observable<RedeemResponse>

    @POST("posts/engagement")
    fun impression(@Body request: ImpressionRequest): Observable<ImpressionResponse>

    @POST("product/merchant")
    fun detailProduct(@Body request: DetailProductRequest): Observable<DetailProductResponse>

    @POST("order/coupon")
    fun discount(@Body request: DiscountRequest): Observable<DiscountResponse>

    @POST("product/category")
    fun getCategoryProduct(@Body request: CategoryRequest): Observable<CategoryResponse>

    @POST("product/subcategory")
    fun getProduct(@Body request: ProductCategoryRequest): Observable<ProductCategoryResponse>

    @POST("product/master")
    fun productItemDetail(@Body request: ProductMasterRequest): Observable<ProductMasterResponse>

    @POST("order/post")
    fun sendPayment(@Body request: PaymentRequest): Observable<BuyResponse>

    @GET("order/paymentchannel")
    fun getPaymentMethod(): Observable<PaymentMethodResponse>

    @POST("order/history")
    fun getHistoryOrder(@Body request: OrderRequest): Observable<OrderResponse>

    @POST("order/detail")
    fun getOrder(@Body request: DetailOrderRequest): Observable<OrderDetailResponse>

    @POST("order/cancel")
    fun cancelOrder(@Body request: DetailOrderRequest): Observable<OrderDetailResponse>
}

