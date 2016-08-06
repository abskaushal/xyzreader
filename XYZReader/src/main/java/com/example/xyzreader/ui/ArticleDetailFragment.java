package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ShareCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";

    private long mItemId;
    private View mRootView;
    private ImageView mPhotoView;
    private boolean mIsCard = false;

    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private TextView mTitle;
    private TextView mAuthor;
    private Toolbar mToolbar;
    private TextView mBody;
    private ImageButton mShare;
    private LinearLayout mTextBg;
    private CardView mCard;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }

        mIsCard = getResources().getBoolean(R.bool.detail_is_card);
        setHasOptionsMenu(true);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);

        bindViews();

        return mRootView;
    }

    private void bindViews(){

        mCollapsingToolbarLayout =  (CollapsingToolbarLayout) mRootView.findViewById(R.id.collapsing_toolbar_layout);
        mPhotoView = (ImageView) mRootView.findViewById(R.id.photo);
        mTitle = (TextView)mRootView.findViewById(R.id.title);
        mAuthor = (TextView) mRootView.findViewById(R.id.author);
        mToolbar = (Toolbar) mRootView.findViewById(R.id.toolbar);
        mShare = (ImageButton) mRootView.findViewById(R.id.share_fab);
        mBody = (TextView) mRootView.findViewById(R.id.body);
        mTextBg = (LinearLayout) mRootView.findViewById(R.id.text_bg);
        mCard = (CardView) mRootView.findViewById(R.id.card_view);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        {
            if (cursor == null || cursor.isClosed() || !cursor.moveToFirst()) {
                return;
            }

            final String title = cursor.getString(ArticleLoader.Query.TITLE);
            String author = Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            cursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by "
                            + cursor.getString(ArticleLoader.Query.AUTHOR)).toString();
            final String body = Html.fromHtml(cursor.getString(ArticleLoader.Query.BODY)).toString();
            String photo = cursor.getString(ArticleLoader.Query.PHOTO_URL);

            if (mToolbar != null) {
                if (mCard == null) {
                    mToolbar.setTitle(title);
                }
                mToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
                mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().finish();
                    }
                });
            }

            mTitle.setText(title);
            mAuthor.setText(author);
            mBody.setText(body);

            Glide.with(this)
                    .load(photo)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model,
                                                       Target<GlideDrawable> target,
                                                       boolean isFromMemoryCache, boolean isFirstResource) {
                            Bitmap bitmap = ((GlideBitmapDrawable) resource.getCurrent()).getBitmap();
                            fillTextBg(bitmap);
                            return false;
                        }
                    })
                    .into(mPhotoView);

            mShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                            .setType("text/plain")
                            .setText(body)
                            .getIntent(), getString(R.string.action_share)));
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        bindViews();
    }

    private void fillTextBg(Bitmap bitmap) {
        Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette palette) {
                int defaultColor = 0xFF333333;
                int darkMutedColor = palette.getDarkMutedColor(defaultColor);
                int mutedColor = palette.getMutedColor(defaultColor);
                mTextBg.setBackgroundColor(darkMutedColor);
                if (mCollapsingToolbarLayout != null) {
                    mCollapsingToolbarLayout.setContentScrimColor(mutedColor);
                    mCollapsingToolbarLayout.setStatusBarScrimColor(darkMutedColor);
                }
            }
        });
    }

}
