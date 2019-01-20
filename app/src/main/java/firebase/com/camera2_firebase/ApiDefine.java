package firebase.com.camera2_firebase;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

public interface ApiDefine {
    @FormUrlEncoded()
    @POST("/add_person.php")
    public void postImage(
            @Field("name") String name,
            @Field("image") String image,
            Callback<Response> response
    );

    @FormUrlEncoded()
    @POST("/train.php")
    public void startTraining(
            @Field("start") boolean start,
            Callback<Response> response
    );

    @FormUrlEncoded()
    @POST("/recognize.php")
    public void recognize(
            @Field("recognize") String recognize,
            Callback<Response> response
    );
}
