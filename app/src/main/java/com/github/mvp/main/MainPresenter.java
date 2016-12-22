package com.github.mvp.main;

import android.content.Context;

import com.github.mvp.data.RootEntity;
import com.github.mvp.data.StoriesEntity;
import com.github.mvp.data.source.retrofit2service.ZhiHuService;

import java.util.ArrayList;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/7/18 0018.
 */
public class MainPresenter implements MainContract.Presenter {

    private String baseUrl = "http://news-at.zhihu.com";

    //private final UserRepository mUserRepository;
    private MainContract.View mMainView;
    private Context mContext;

    //要靠他来获取消息
    protected ZhiHuService service;

    public MainPresenter(Context context) {
        this.mContext = context;
    }

    public void setView(MainContract.View view) {
        this.mMainView = view;
        mMainView.setPresenter(this);
        mMainView.setTitle();
        service = getService();
    }

    public ZhiHuService getService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        service = retrofit.create(ZhiHuService.class);
        return service;
    }

    @Override
    public RootEntity getLatestNews() {
        return loadData(service.getLatestNews());
    }

    @Override
    public RootEntity getSafety() {
        return loadData(service.getSafety());
    }

    @Override
    public RootEntity getInterest() {
        return loadData(service.getInterest());
    }

    @Override
    public RootEntity getSport() {
        return loadData(service.getSport());
    }

    @Override
    public void start() {

    }

    public RootEntity loadData(Observable<RootEntity> observable) {
        final RootEntity rootEntity = new RootEntity();
        observable.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .map(new Func1<RootEntity, ArrayList<StoriesEntity>>() {
                    @Override
                    public ArrayList<StoriesEntity> call(RootEntity rootEntity) {
                        return rootEntity.getStories();
                    }
                })
                .subscribe(new Subscriber<ArrayList<StoriesEntity>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ArrayList<StoriesEntity> storiesEntities) {
                        rootEntity.setStories(storiesEntities);
                        mMainView.refresh(storiesEntities);
                    }
                });

        return rootEntity;
    }
}
