/**
 * TuSDKVideoDemo
 * MultipleCameraView.java
 *
 * @author     LiuHang
 * @Date:      Jun 3, 2017 3:48:19 PM
 * @Copyright: (c) 2017 tusdk.com. All rights reserved.
 *
 */
package org.lasque.tusdkvideodemo.views.record;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lasque.tusdk.core.TuSdk;
import org.lasque.tusdk.core.TuSdkContext;
import org.lasque.tusdk.core.seles.sources.SelesOutInput;
import org.lasque.tusdk.core.seles.sources.SelesVideoCameraInterface;
import org.lasque.tusdk.core.seles.tusdk.FilterManager;
import org.lasque.tusdk.core.struct.TuSdkSize;
import org.lasque.tusdk.core.utils.FileHelper;
import org.lasque.tusdk.core.utils.RectHelper;
import org.lasque.tusdk.core.utils.TLog;
import org.lasque.tusdk.core.utils.hardware.CameraConfigs.CameraFlash;
import org.lasque.tusdk.core.utils.hardware.TuSDKRecordVideoCamera;
import org.lasque.tusdk.core.utils.hardware.TuSDKRecordVideoCamera.RecordError;
import org.lasque.tusdk.core.utils.hardware.TuSDKRecordVideoCamera.RecordState;
import org.lasque.tusdk.core.utils.image.AlbumHelper;
import org.lasque.tusdk.core.utils.sqllite.ImageSqlHelper;
import org.lasque.tusdk.core.video.TuSDKVideoResult;
import org.lasque.tusdk.core.view.recyclerview.TuSdkTableView;
import org.lasque.tusdk.core.view.recyclerview.TuSdkTableView.TuSdkTableViewItemClickDelegate;
import org.lasque.tusdk.core.view.widget.button.TuSdkTextButton;
import org.lasque.tusdk.impl.view.widget.TuSeekBar;
import org.lasque.tusdk.modules.view.widget.sticker.StickerGroup;
import org.lasque.tusdk.modules.view.widget.sticker.StickerLocalPackage;
import org.lasque.tusdk.movie.player.TuSDKMoviePlayer;
import org.lasque.tusdk.movie.player.TuSDKMoviePlayer.PlayerState;
import org.lasque.tusdk.movie.player.TuSDKMoviePlayer.TuSDKMoviePlayerDelegate;
import org.lasque.tusdkvideodemo.R;
import org.lasque.tusdkvideodemo.utils.ClickAndLongPressedInterface;
import org.lasque.tusdkvideodemo.utils.ClickAndLongPressedListener;
import org.lasque.tusdkvideodemo.utils.Constants;
import org.lasque.tusdkvideodemo.views.CompoundDrawableTextView;
import org.lasque.tusdkvideodemo.views.FilterCellView;
import org.lasque.tusdkvideodemo.views.FilterConfigView;
import org.lasque.tusdkvideodemo.views.FilterListView;
import org.lasque.tusdkvideodemo.views.RoundProgressBar;
import org.lasque.tusdkvideodemo.views.StickerCellView;
import org.lasque.tusdkvideodemo.views.StickerListView;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 多功能相机界面视图
 */
public class MultipleCameraView extends FrameLayout
{
	/** 资源类型--图片类型 */
	private final int RES_TYPE_IMAGE = 0;
	/** 资源类型--视频类型 */
	private final int RES_TYPE_VIDEO = 1;
	
	/**相机 */
    protected TuSDKRecordVideoCamera mTuSDKVideoCamera;
    
	/** 自定义的圆形进度条*/
	private RoundProgressBar mRoundProgressBar;
	/**返回按钮 */
	private ImageButton mBackButton;
	/**相机镜头切换按钮 */
	private ImageButton mToggleButton;
	/**闪光灯按钮 */
	private ImageButton mFlashButton;
	/**滤镜按钮 */
	private CompoundDrawableTextView mFilterButton;
	/**动态贴纸按钮 */
	private CompoundDrawableTextView mStickerButton;
	/**闪光灯状态 */
	private boolean mFlashEnabled = false;
	/** 在第一次运行的时候选中无效果滤镜*/
	private boolean isFirstTimeRun;

	/** 上方的触摸视图 */
	private FrameLayout mTouchView;
	/** 参数调节视图 */
	protected FilterConfigView mConfigView;
	/** 滤镜栏视图 */
	protected FilterListView mFilterListView;
	/** 贴纸栏视图 */
	protected StickerListView mStickerListView;
	/** 滤镜底部栏 */
	private RelativeLayout mFilterBottomView;
	/** 贴纸底部栏 */
	private RelativeLayout mStickerBottomView;
	/** 美颜按钮 */
	private TuSdkTextButton mBeautyBtn;
	/** 美颜布局 */
	private RelativeLayout mBeautyLayout;
	/** 美颜磨皮强度调节栏 */
	private TuSeekBar mBeautyBar;
	/** 磨皮强度值 */
	private TextView mBeautyLevel;
	/** 底部按钮布局 */
	private RelativeLayout mBottomBtnLayout;
	/** 预览图片控件 */
	private ImageView mPreviewImg;
	/** 播放视频SurfaceView */
	private SurfaceView mSurfaceView;
	/** 预览界面删除按钮 */
	private CompoundDrawableTextView mDeltButton;
	/** 预览界面保存按钮 */
	private CompoundDrawableTextView mSaveButton;
	/** 预览布局 */
	private RelativeLayout mPreviewLayout;
	/** 预览视频布局 */
	private RelativeLayout mPreviewVideoLayout;
	/** 处理的资源类型：默认图片类型 */
	private int mResType = RES_TYPE_IMAGE;
	/** 视频资源路径 */ 
	private String mVideoPath;
	/** 视频播放器 */
	private TuSDKMoviePlayer mMoviePlayer;
	/** 拍照获得的Bitmap */
	private Bitmap mCaptureBitmap;
	/** 上一次选中的贴纸项 */
	private RelativeLayout mLastStickerView;
	/** 上一个选中的滤镜 */
	private FilterCellView mLastSelectedCellView;
	private Activity mActivity;
	private ClickAndLongPressedListener mClickAndLongPressListener = new ClickAndLongPressedListener();
	/** 录制视频动作委托 */
	private TuSDKMultipleCameraDelegate mDelegate;
	/** 美颜强度调节栏 */
	private RelativeLayout mBeautyBarWrap;
	
	/**
	 * 录制视频动作委托
	 */
	public interface TuSDKMultipleCameraDelegate
	{
		/**
		 * 暂停相机
		 */
		void pauseCameraCapture();
		
		/**
		 * 恢复相机
		 */
		void resumeCameraCapture();
		
		/**
		 * 视频成功通知
		 */
		void onMovieSaveSucceed(String videoPath);
	}
	
	public MultipleCameraView(Context context) 
	{
		super(context);
	}

	public MultipleCameraView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		initView(context);
	}
	
	public void setDelegate(TuSDKMultipleCameraDelegate delegate)
	{
		mDelegate = delegate;
	}
	
	public TuSDKMultipleCameraDelegate getDelegate()
	{
		return mDelegate;
	}
	
	@SuppressLint( "ClickableViewAccessibility") 
	private void initView(Context context)
	{
		//圆形进度条
		LayoutInflater.from(context).inflate(R.layout.multiple_camera_view, this,true);
		mRoundProgressBar = (RoundProgressBar) findViewById(R.id.roundProgressBar);
		mClickAndLongPressListener.setLongPressedAndClickInterface(mClickAndLongPressedInterface);
		mRoundProgressBar.setOnTouchListener(mClickAndLongPressListener);
		
		//顶部按钮
		mBackButton = (ImageButton) findViewById(R.id.lsq_back_btn);
		mToggleButton = (ImageButton) findViewById(R.id.lsq_toggle_btn);
		mFlashButton = (ImageButton) findViewById(R.id.lsq_flash_btn);
		mBackButton.setOnClickListener(mButtonListener);
		mToggleButton.setOnClickListener(mButtonListener);
		mFlashButton.setOnClickListener(mButtonListener);

		//贴纸视图
		mStickerBottomView = (RelativeLayout) findViewById(R.id.lsq_sticker_group_bottom_view);
		mStickerBottomView.setVisibility(View.INVISIBLE);
		//点击切换贴纸、滤镜视图
		mTouchView = (FrameLayout) findViewById(R.id.lsq_touch_view);
		mTouchView.setOnClickListener(mButtonListener);
		//滤镜视图
		mFilterBottomView = (RelativeLayout) findViewById(R.id.lsq_filter_group_bottom_view);
		mFilterBottomView.setVisibility(View.INVISIBLE);

		//底部按钮
		mBottomBtnLayout = (RelativeLayout) findViewById(R.id.lsq_btn_layout);
		mFilterButton = (CompoundDrawableTextView) findViewById(R.id.lsq_filter_btn);
		mStickerButton = (CompoundDrawableTextView) findViewById(R.id.lsq_sticker_btn);
		mStickerButton.setOnTouchListener(mOnTouchListener);
		mFilterButton.setOnTouchListener(mOnTouchListener);
		//滤镜视图中开启美颜按钮
		mBeautyLayout = (RelativeLayout) findViewById(R.id.lsq_beautyWrap);
		mBeautyLayout.setOnClickListener(mButtonListener);
		
		//美颜磨皮强度视图
		mBeautyBtn = (TuSdkTextButton) findViewById(R.id.lsq_beautyBtn);
		
		mBeautyBar = (TuSeekBar) findViewById(R.id.lsq_seekBar);
		mBeautyBar.setDelegate(mTuSeekBarDelegate);
		getBeautyBarWrap().setVisibility(View.INVISIBLE);
		mBeautyLevel = (TextView) findViewById(R.id.lsq_level_View);
		
		//预览图片控件
		mPreviewImg = (ImageView) findViewById(R.id.lsq_preview_imageview);
		mPreviewImg.setVisibility(View.GONE);
		
		mPreviewLayout = (RelativeLayout) findViewById(R.id.lsq_preview);
		mPreviewLayout.setVisibility(View.INVISIBLE);
		mPreviewVideoLayout = (RelativeLayout) findViewById(R.id.lsq_preview_video);
		mDeltButton = (CompoundDrawableTextView) findViewById(R.id.lsq_delet_btn);
		mSaveButton = (CompoundDrawableTextView) findViewById(R.id.lsq_save_btn);
		mDeltButton.setOnTouchListener(mOnTouchListener);
		mSaveButton.setOnTouchListener(mOnTouchListener);
		
		initStickerListView();
		initFilterListView();
		
		// 设置弹窗提示是否在隐藏虚拟键的情况下使用
		TuSdk.messageHub().applyToViewWithNavigationBarHidden(true);
	}
	
	/**
	 * 初始化滤镜栏视图
	 */
	protected void initFilterListView() 
	{
		getFilterListView();

		if (mFilterListView == null)
			return;

		this.mFilterListView.setModeList(Arrays.asList(Constants.VIDEOFILTERS));
	}
	
	/**
	 * 滤镜栏视图
	 * 
	 * @return
	 */
	private FilterListView getFilterListView() 
	{
		if (mFilterListView == null) {
			mFilterListView = (FilterListView) findViewById(R.id.lsq_filter_list_view);
			mFilterListView.loadView();
			mFilterListView.setCellLayoutId(R.layout.filter_list_cell_view);
			mFilterListView.setCellWidth(TuSdkContext.dip2px(80));
			mFilterListView.setItemClickDelegate(mFilterTableItemClickDelegate);
			mFilterListView.reloadData();
		}
	
		return mFilterListView;
	}
	
	/** 滤镜组列表点击事件 */
	private TuSdkTableViewItemClickDelegate<String, FilterCellView> mFilterTableItemClickDelegate = new TuSdkTableViewItemClickDelegate<String, FilterCellView>() {
		@Override
		public void onTableViewItemClick(String itemData,
				FilterCellView itemView, int position) {
			onFilterGroupSelected(itemData, itemView, position);
		}
	};
	
	/**
	 * 滤镜组选择事件
	 * 
	 * @param itemData
	 * @param itemView
	 * @param position
	 */
	protected void onFilterGroupSelected(String itemData,
			FilterCellView itemView, int position) 
	{
		changeVideoFilterCode(itemData);

		deSelectLastFilter(mLastSelectedCellView, position);

		selectFilter(itemView, position);
		
        getFilterConfigView().setVisibility(
                FilterManager.shared().isNormalFilter(itemData) || (getFilterConfigView().getVisibility() == View.INVISIBLE
                && getBeautyBarWrap().getVisibility() == View.INVISIBLE) ? View.INVISIBLE : View.VISIBLE);	

		// 更改美颜状态
		updateBeautyStatus(false);
    }
	
	/**
	 * 切换滤镜
	 * 
	 * @param code
	 */
	protected void changeVideoFilterCode(String code) 
	{
		mTuSDKVideoCamera.switchFilter(code);
	}
	
	/**
	 * 取消上一个滤镜的选中状态
	 * 
	 * @param lastFilter
	 * @param position
	 */
	private void deSelectLastFilter(FilterCellView lastFilter, int position)
	{
		if (lastFilter == null) return;

		lastFilter.setFlag(-1);
		updateFilterBorderView(lastFilter,true);
		lastFilter.getTitleView().setBackground(TuSdkContext.getDrawable("tusdk_view_filter_unselected_text_roundcorner"));
		lastFilter.getImageView().invalidate();
	}
	
	/**
	 * 初始化贴纸组视图
	 */
	protected void initStickerListView() 
	{
		getStickerListView();

		if (mStickerListView == null) return;

		List<StickerGroup> groups = new ArrayList<StickerGroup>();
		groups.addAll(StickerLocalPackage.shared().getSmartStickerGroups());

		groups.add(0, new StickerGroup());
		this.mStickerListView.setModeList(groups);
	}
	
	/**
	 * 贴纸组视图
	 */
	private StickerListView getStickerListView() 
	{
		if (mStickerListView == null) {
			mStickerListView = (StickerListView) findViewById(R.id.lsq_sticker_list_view);
			mStickerListView.loadView();
			mStickerListView.setCellLayoutId(R.layout.sticker_list_cell_view);
			GridLayoutManager grid = new GridLayoutManager(mActivity, 5);
			mStickerListView.setLayoutManager(grid);
			mStickerListView.setCellWidth(TuSdkContext.dip2px(80));
			mStickerListView
					.setItemClickDelegate(mStickerTableItemClickDelegate);
			mStickerListView.reloadData();
		}
		return mStickerListView;
	}
	
	/**
	 * 贴纸组列表点击事件
	 */
	private TuSdkTableView.TuSdkTableViewItemClickDelegate<StickerGroup, StickerCellView> mStickerTableItemClickDelegate = new TuSdkTableView.TuSdkTableViewItemClickDelegate<StickerGroup, StickerCellView>() {
		@Override
		public void onTableViewItemClick(StickerGroup itemData,
				StickerCellView itemView, int position) {
			onStickerGroupSelected(itemData, itemView, position);
		}
	};
	
	/**
	 * 贴纸组选择事件
	 * 
	 * @param itemData
	 * @param itemView
	 * @param position
	 */
	protected void onStickerGroupSelected(StickerGroup itemData,
			StickerCellView itemView, int position) 
	{
		// 清除上一个选中贴纸的背景
		if (mLastStickerView != null) {
			mLastStickerView.setBackground(null);
		}

		RelativeLayout stickerLayout = (RelativeLayout) itemView
				.findViewById(R.id.lsq_item_wrap);
		stickerLayout.setBackground(TuSdkContext
				.getDrawable(R.drawable.sticker_cell_background));

		// 记录下选中的贴纸
		mLastStickerView = stickerLayout;

		// 设置点击贴纸时呈现或是隐藏贴纸
		if (position == 0) {
			mTuSDKVideoCamera.removeAllLiveSticker();
		} else {
			mTuSDKVideoCamera.showGroupSticker(itemData);
		}
	}
	
	/**
	 * 传递录制相机对象
	 * 
	 * @param camera
	 */
	public void setUpCamera(Activity activity,TuSDKRecordVideoCamera tuSDKRecordVideoCamera)
	{
		this.mActivity = activity;
		this.mTuSDKVideoCamera = tuSDKRecordVideoCamera;
	}
	
	private View.OnClickListener mButtonListener=new View.OnClickListener() 
	{
		@Override
		public void onClick(View v)
		{
			if (v == mBackButton)
			{
				mActivity.finish();
			}
			else if (v == mFlashButton)
			{
				mFlashEnabled = !mFlashEnabled;
	
				mTuSDKVideoCamera.setFlashMode(mFlashEnabled ? CameraFlash.Torch
						: CameraFlash.Off);
	
				// 闪光灯
				updateButtonStatus(mFlashButton, mFlashEnabled);
			}
			else if (v == mToggleButton)
			{
				mTuSDKVideoCamera.rotateCamera();
			}
			else if (v == mTouchView)
			{
				hideStickerStaff();
				hideFilterStaff();
				handleBottomBtnLayout(false);
			}
			else if (v == mBeautyLayout)
			{
				getBeautyBarWrap().setVisibility(View.VISIBLE);
				getBeautyBar().setProgress(mTuSDKVideoCamera.getBeautyLevel());
				updateButtonStatus(mBeautyBtn, mTuSDKVideoCamera.getBeautyLevel() > (float)0);
				updateBeautyStatus(true);

				// 隐藏滤镜调节栏
				getFilterConfigView().setVisibility(View.INVISIBLE);
				// 隐藏滤镜选中边框
				updateFilterBorderView(mLastSelectedCellView, true);
			}
		  }
	 };
	 
	/**
	 * 更新按钮显示状态
	 * 
	 * @param button
	 * @param clickable
	 */
	private void updateButtonStatus(TuSdkTextButton button, boolean clickable)
	{
		int imgId = 0, colorId = 0;
		
		switch (button.getId())
		{
		case R.id.lsq_beautyBtn:
			imgId = clickable ? R.drawable.lsq_style_default_btn_beauty_selected
					: R.drawable.lsq_style_default_btn_beauty_unselected;
			colorId = clickable ? R.color.lsq_filter_title_color
					: R.color.lsq_filter_title_default_color;
			break;

		default:
			break;
		}
		
		button.setCompoundDrawables(null, TuSdkContext.getDrawable(imgId), null, null);
		button.setTextColor(TuSdkContext.getColor(colorId));
	}
		
	/**
	 * 更新按钮显示状态
	 * 
	 * @param button
	 * @param clickable
	 */
	private void updateButtonStatus(ImageButton button, boolean clickable)
	{
		int imgId = 0;

		switch (button.getId()) {

		case R.id.lsq_flash_btn:
			imgId = clickable ? R.drawable.lsq_flash_open
					: R.drawable.lsq_flash_closed;
			break;

		case R.id.lsq_beautyBtn:
			imgId = clickable ? R.drawable.lsq_style_default_btn_beauty_selected
					: R.drawable.lsq_style_default_btn_beauty_unselected;

		default:
			break;
		}
		button.setImageResource(imgId);
	}
	
	/**
	 * 点击贴纸栏上方的空白区域隐藏贴纸栏
	 */
	public void hideStickerStaff() 
	{
		if (mStickerBottomView.getVisibility() == View.INVISIBLE)
			return;

		updateStickerViewStaff(false);

		// 滤镜栏向下动画并隐藏
		ViewCompat.animate(mStickerBottomView)
				.translationY(mStickerBottomView.getHeight()).setDuration(200);
	}
	
	/**
	 * 点击滤镜栏上方的空白区域隐藏滤镜栏
	 */
	public void hideFilterStaff() 
	{
		if (mFilterBottomView.getVisibility() == View.INVISIBLE) return;

		updateFilterViewStaff(false);

		// 滤镜栏向下动画并隐藏
		ViewCompat.animate(mFilterBottomView)
				.translationY(mFilterBottomView.getHeight()).setDuration(200);
	}
	
	/**
	 * 处理底部按钮的显示状态
	 * 
	 * @param isHidden
	 */
	private void handleBottomBtnLayout(boolean isHidden) 
	{
		mBottomBtnLayout.setVisibility(isHidden ? View.INVISIBLE : View.VISIBLE);
	}
	
	/**
	 * 更新美颜按钮、拖动条显示状态
	 * 
	 * @param clickable
	 */
	private void updateBeautyStatus(boolean clickable) 
	{
		int imgId = clickable ? R.drawable.tusdk_view_beauty_roundcorner_selected_bg
				: R.drawable.tusdk_view_beauty_roundcorner_unselected_bg;
		mBeautyLayout.setBackgroundResource(imgId);
		getBeautyBarWrap().setVisibility(clickable ? View.VISIBLE : View.INVISIBLE);
	}
	
	private ClickAndLongPressedInterface mClickAndLongPressedInterface = new ClickAndLongPressedInterface()
	{
		@Override
		public void onLongPressedDown() 
		{
			if (mTuSDKVideoCamera == null) return;
			
			mTuSDKVideoCamera.startRecording();
			setBackButtonHided(true);
		}

		@Override
		public void onLongPressedUp() 
		{
			mResType = RES_TYPE_VIDEO;
			if (mTuSDKVideoCamera != null)
			{
				mTuSDKVideoCamera.stopRecording();
				setBackButtonHided(false);
			}
		}
		
		@Override
		public void onClick() 
		{
			updateCaptureState();
			hideFilterStickerBtn(true);
			if (mTuSDKVideoCamera != null)
			{
				mTuSDKVideoCamera.captureImage();
			}
		}
	};
	
	@SuppressLint("ClickableViewAccessibility") 
	private View.OnTouchListener mOnTouchListener = new OnTouchListener()
	{
		@Override
		public boolean onTouch(View v, MotionEvent event) {
		
			// 只响应ACTION_DOWN事件
			if (event.getAction() != MotionEvent.ACTION_DOWN) return false;

			if (v == mStickerButton)
			{
				handleStickerButton();
				changeTouchViewSize(mStickerBottomView);
			}
			else if (v == mFilterButton)
			{
				handleFilterButton();
				changeTouchViewSize(mFilterBottomView);
			}
			else if (v == mDeltButton) 
			{
				deleteResource();
			}
			else if (v == mSaveButton)
			{
				saveResource();
			}
			return true;
		}
	};
	
	/** 美颜拖动条监听事件 */
	private TuSeekBar.TuSeekBarDelegate mTuSeekBarDelegate = new TuSeekBar.TuSeekBarDelegate()
	{

        @Override
        public void onTuSeekBarChanged(TuSeekBar seekBar, float progress) 
        {
          mBeautyLevel.setText((int)(progress*100) + "%");
          mTuSDKVideoCamera.setBeautyLevel(progress);

          if (progress <= 0)
          {
              updateButtonStatus(mBeautyBtn, false);
          }
          else
          {
              updateButtonStatus(mBeautyBtn, true);
          }
        }
	};
	
	/**
	 * 更新贴纸栏相关视图的显示状态
	 * 
	 * @param isShow
	 */
	private void updateStickerViewStaff(boolean isShow)
	{
		mStickerBottomView
				.setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
		mTouchView.setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
	}
	
	/**
	 * 更新滤镜栏相关视图的显示状态
	 * 
	 * @param isShow
	 */
	private void updateFilterViewStaff(boolean isShow)
	{
		mFilterBottomView.setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
		mTouchView.setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
	}
	
	/**
	 * 滤镜配置视图
	 * 
	 * @return
	 */
	private FilterConfigView getFilterConfigView() 
	{
	    if (mConfigView == null)
        {
            mConfigView = (FilterConfigView) findViewById(R.id.lsq_filter_config_view);
        }

        return mConfigView;
	} 
	
	/**
	 * 美颜调节SeekBar
	 * @return
	 */
	private TuSeekBar getBeautyBar()
	{
		if(mBeautyBar == null)
		{
			mBeautyBar = (TuSeekBar) findViewById(R.id.lsq_seekBar);
			mBeautyBar.setDelegate(mTuSeekBarDelegate);
		}
		return mBeautyBar;
	}
	
	/**
	 * 美颜调节栏配置视图
	 * @return
	 */
	private RelativeLayout getBeautyBarWrap()
	{
		if(mBeautyBarWrap == null)
		{
			mBeautyBarWrap = (RelativeLayout) findViewById(R.id.lsq_beauty_config_view);
		}
		return mBeautyBarWrap;
	}
	
	/**
	 * 设置返回按钮是否隐藏
	 * @param isHidden
	 */
    private void setBackButtonHided(boolean isHidden)
    {
		if (mBackButton == null){
			TLog.e("mBackButton == null");
			return;
		}
    	mBackButton.setVisibility(isHidden?View.INVISIBLE:View.VISIBLE);
    }
    
	/**
	 * 更新拍照进度条状态
	 */
	private void updateCaptureState()
	{
		int innerRoundColor = TuSdkContext.getColor("lsq_capture_selected");
	    mRoundProgressBar.setInnerRoundColor(innerRoundColor);
	    mRoundProgressBar.invalidate();
	}
	
	/** 显示贴纸视图 */
	protected void handleStickerButton() 
	{
		showStickerLayout();
		handleBottomBtnLayout(true);
	}
	
	 /**
	  * 改变控制贴纸、滤镜动画的点击区域大小
	  */
	 private void changeTouchViewSize(View v)
	 {
		FrameLayout.LayoutParams touchViewLayoutParams = (LayoutParams) mTouchView.getLayoutParams();
		touchViewLayoutParams.setMargins(0, TuSdkContext.dip2px(60), 0,v.getHeight());
	 }
	 
	private void hideFilterStickerBtn(boolean isHidden)
	{
		mStickerButton.setVisibility(isHidden ? View.INVISIBLE : View.VISIBLE);
		mFilterButton.setVisibility(isHidden ? View.INVISIBLE : View.VISIBLE);
	}
	
	/** 显示滤镜视图 */
	protected void handleFilterButton()
	{
		showFilterLayout();
		handleBottomBtnLayout(true);
	}
	
    /**
     * 销毁图片
     */
    private void destroyBitmap()
    {
    	if (mCaptureBitmap == null) return;
    	
    	if (!mCaptureBitmap.isRecycled())
    		mCaptureBitmap.recycle();
    	
    	mCaptureBitmap = null;
    }
    
    /**
     * 删除资源
     */
    public void deleteResource()
    {
    	
		if (mPreviewLayout == null|| mPreviewVideoLayout == null)
		{
			TLog.e("mPreviewLayout == null || mPreviewVideoLayout == null");
			return;
		}		 
		
		if(mResType == RES_TYPE_IMAGE)
		{
			updateImagePreviewStatus(false);
			destroyBitmap();
            TuSdk.messageHub().showToast(mActivity, R.string.lsq_image_del_ok);
		}
		
		if(mResType == RES_TYPE_VIDEO && mMoviePlayer != null && mSurfaceView != null)
		{
			// 返回录制界面需先调用removeAllViews清除预览画面
			// 不然无法显示相机预览界面
			mPreviewVideoLayout.removeAllViews();
			
			updateVideoPreviewStatus(false);
			mMoviePlayer.stop();
			File file = new File(mVideoPath);
			FileHelper.delete(file);
			refreshFile(file);
            TuSdk.messageHub().showToast(mActivity, R.string.lsq_video_del_ok);

		}
		hideNavigationBar();
		resetProgess();
		showStickerAndFilter();
		getDelegate().resumeCameraCapture();
		updateProgressBarState(false);
	}

	/**
     * 隐藏虚拟按键，并且全屏
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
	protected void hideNavigationBar() 
    {
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) 
        { 
            View decorview = mActivity.getWindow().getDecorView();
            decorview.setSystemUiVisibility(View.GONE);
        } 
        else if (Build.VERSION.SDK_INT >= 19)
        {
            View decorView = mActivity.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }
    
    /**
     * 保存资源
     */
	public void saveResource()
	{
		if (mPreviewLayout == null|| mPreviewVideoLayout == null)
		{
			TLog.e("mPreviewLayout == null || mPreviewVideoLayout == null");
			return;
		}

		if(mResType == RES_TYPE_IMAGE)
		{
            File flie = AlbumHelper.getAlbumFile();
            ImageSqlHelper.saveJpgToAblum(mActivity, mCaptureBitmap, 0, flie);
            refreshFile(flie);
			updateImagePreviewStatus(false);
            destroyBitmap();
            TuSdk.messageHub().showToast(mActivity, R.string.lsq_image_save_ok);
		}
	
		if(mResType == RES_TYPE_VIDEO && mMoviePlayer != null && mSurfaceView != null)
		{
			// 返回录制界面需先调用removeAllViews清除预览画面
			// 不然无法显示相机预览界面
			mPreviewVideoLayout.removeAllViews();
			
			updateVideoPreviewStatus(false);
			mMoviePlayer.stop();
			ImageSqlHelper.saveMp4ToAlbum(mActivity,new File(mVideoPath));
			refreshFile(new File(mVideoPath));
			getDelegate().onMovieSaveSucceed(mVideoPath);
            TuSdk.messageHub().showToast(mActivity, R.string.lsq_video_save_ok);
		}
		hideNavigationBar();
		resetProgess();
		showStickerAndFilter();
		getDelegate().resumeCameraCapture();
		updateProgressBarState(false);
	 }
	 
		/**
		 * 显示贴纸底部栏
		 */
		public void showStickerLayout() 
		{
			updateStickerViewStaff(true);

			// 滤镜栏向上动画并显示
			ViewCompat.setTranslationY(mStickerBottomView,
					mStickerBottomView.getHeight());
			ViewCompat.animate(mStickerBottomView).translationY(0).setDuration(200);
		}
		
		/**
		 * 显示滤镜底部栏
		 */
		public void showFilterLayout() 
		{
			// 第一次进入滤镜页面默认选中无效果滤镜
			if (!isFirstTimeRun) {
				FilterCellView firstItem = (FilterCellView) mFilterListView
						.getChildAt(0);
				selectFilter(firstItem, 0);
				mLastSelectedCellView = firstItem;
				isFirstTimeRun = true;
			}
			
			getFilterConfigView().setVisibility(View.INVISIBLE);
			
			updateFilterViewStaff(true);

			// 滤镜栏向上动画并显示
			ViewCompat.setTranslationY(mFilterBottomView,
					mFilterBottomView.getHeight());
			ViewCompat.animate(mFilterBottomView).translationY(0).setDuration(200);
		}
		
		/**
		 * 更新图片预览界面的状态
		 * @param isShowed
		 */
		private void updateImagePreviewStatus(boolean isShowed)
		{
			mPreviewLayout.setVisibility(isShowed ? View.VISIBLE : View.GONE);
			mPreviewImg.setVisibility(isShowed ?View.VISIBLE :View.GONE);
		}
		/**
		 * 更新视频预览界面的状态
		 * @param isShowed
		 */
		private void updateVideoPreviewStatus(boolean isShowed)
		{
			mPreviewLayout.setVisibility(isShowed ? View.VISIBLE :View.GONE);
			mPreviewVideoLayout.setVisibility(isShowed ? View.VISIBLE :View.GONE);
			mSurfaceView.setZOrderMediaOverlay(isShowed ? true :false);
			mPreviewImg.setVisibility(View.GONE);
		}
		
		 public void refreshFile(File file) 
		 {
			if (file == null) {
				TLog.e("refreshFile file == null");
				return;
			}
			
			Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			Uri uri = Uri.fromFile(file);
			intent.setData(uri);
			mActivity.sendBroadcast(intent);
		 }
		 
	/**
	 * 录制结束重置进度条状态
	 */
	private void resetProgess()
	{
		if (mRoundProgressBar == null)
		{
			TLog.e("mRoundProgressBar == null");
			return;
		}
		
	    int innerRoundColor= TuSdkContext.getColor("lsq_innerRound_unselected_color");
        mRoundProgressBar.setInnerRoundColor(innerRoundColor);
        mRoundProgressBar.setRingWidth(TuSdkContext.dip2px(6));
        mRoundProgressBar.setInnerRoundRadius(TuSdkContext.dip2px(30));
        mRoundProgressBar.setRingProgresswidth(TuSdkContext.dip2px(85));
        mRoundProgressBar.setProgress(0);
        mRoundProgressBar.invalidate();
	}
	
	/**
	 * 显示贴纸和滤镜
	 */
	private void showStickerAndFilter() 
	{
		if (mStickerButton == null || mFilterButton == null)
		{
			TLog.e("mStickerButton == null || mFilterButton == null");
			return;
		}
		mStickerButton.setVisibility(View.VISIBLE);
		mFilterButton.setVisibility(View.VISIBLE);
	}
	
	/**
	 * 滤镜选中状态
	 * 
	 * @param itemView
	 * @param position
	 */
	private void selectFilter(FilterCellView itemView, int position) 
	{
		updateFilterBorderView(itemView,false);
		itemView.setFlag(position);

		TextView titleView = itemView.getTitleView();
		titleView.setBackground(TuSdkContext.getDrawable("tusdk_view_filter_selected_text_roundcorner"));

		// 记录本次选中的滤镜
		mLastSelectedCellView = itemView;
	}
	
	/**
	 * 设置滤镜单元边框是否可见
	 * @param lastFilter
	 * @param isHidden
	 */
	private void updateFilterBorderView(FilterCellView lastFilter,boolean isHidden)
	{
		RelativeLayout filterBorderView = lastFilter.getBorderView();
		filterBorderView.setVisibility(isHidden ? View.GONE : View.VISIBLE);
	}
	
	/**
	 * 录制错误时更新视图显示
	 * 
	 * @param error
	 * @param isRecording
	 */
	public void updateViewOnMovieRecordFailed(RecordError error)
	{
        TLog.e("RecordError : %s",error);
        if (error == RecordError.InvalidRecordingTime)
            TuSdk.messageHub().showToast(mActivity, R.string.lsq_record_time_invalid);
        else
            TuSdk.messageHub().showToast(mActivity, R.string.lsq_record_movie_error);
        
		resetProgess();
		showStickerAndFilter();
		getDelegate().resumeCameraCapture();
	}
	
	/**
	 * 录制状态发生改变时更新视图显示
	 * 
	 * @param state
	 * @param isRecording
	 */
	public void updateViewOnMovieRecordStateChanged(RecordState state)
	{
        if (state == RecordState.Recording)
        {
            hideFilterStickerBtn(true);
        }
	}
	
	/**
	 * 录制进度改变时更新视图显示
	 * 
	 * @param progress
	 * @param durationTime
	 */
	public void updateViewOnMovieRecordProgressChanged(float progress, float durationTime) 
	{
		updateProgressBar((int)(progress*100));
	}
	
	/**
	 * 录制完成时更新视图显示
	 * 
	 * @param isRecording
	 */
	public void updateViewOnMovieRecordComplete(TuSDKVideoResult result)
	{
		previewCarmeraVideo(result.videoPath.getPath());
		// 暂停相机
		getDelegate().pauseCameraCapture();
	}
	
	/**
	 * 滤镜变化时改变滤镜调节栏
	 * @param selesOutInput
	 */
	public void updateViewOnFilterChanged(SelesOutInput selesOutInput)
	{
      if (selesOutInput != null && getFilterConfigView() != null) 
        {
            getFilterConfigView().setSelesFilter(selesOutInput);
        }
	}
	
	/**
	 * 拍照
	 * @param camera
	 * @param bitmap
	 */
	public void updateViewOnVideoCameraScreenShot(SelesVideoCameraInterface camera, Bitmap bitmap) 
	{
		if(bitmap != null) 
		{
			displayCapturedImage(bitmap);
			// 暂停相机
			camera.pauseCameraCapture();
			
			updateProgressBarState(true);
		}
	}
	
	public void displayCapturedImage(final Bitmap bitmap)
	{
		mResType = RES_TYPE_IMAGE;
		mCaptureBitmap = bitmap;
		
		if (mPreviewLayout == null || mPreviewImg == null) return;
		
		updateImagePreviewStatus(true);
		mPreviewImg.setImageBitmap(bitmap)	;			
	}
	
	/**
	 * 更新进度条进度
	 * @param progress
	 */
	private void updateProgressBar(int progress)
	{
        int innerRoundColor = TuSdkContext.getColor("lsq_innerRound_selected_color");
        mRoundProgressBar.setInnerRoundColor(innerRoundColor);
        mRoundProgressBar.setRingWidth(TuSdkContext.dip2px(8));
        mRoundProgressBar.setInnerRoundRadius(TuSdkContext.dip2px(38));
        mRoundProgressBar.setRingProgresswidth(TuSdkContext.dip2px(102));
        mRoundProgressBar.setProgress(progress);
        mRoundProgressBar.invalidate();
	}
	
	/**
	 * 创建SurfaceView播放视频
	 */
	private void createSurfaceViewOnPreviewLayout()
	{
		mSurfaceView = new SurfaceView(mActivity);
		FrameLayout.LayoutParams params = new FrameLayout
                .LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT);
		mPreviewVideoLayout.setLayoutParams(params);	
		mPreviewVideoLayout.addView(mSurfaceView);
	}
	
	public void previewCarmeraVideo(final String videoPath)
	{
		if (mPreviewLayout == null || mPreviewVideoLayout == null || mPreviewImg == null){
			TLog.e("mPreviewLayout == null || mPreviewVideoLayout == null || mPreviewImg == null");
			return;
		}
	    this.mVideoPath = videoPath;
		mResType = RES_TYPE_VIDEO;
		createSurfaceViewOnPreviewLayout();
		updateVideoPreviewStatus(true);
		updateProgressBarState(true);
		playMovie(videoPath);
	}
	
	/**
	 * 更新RoundProgressBar的状态
	 * @param isHidden
	 */
	private void updateProgressBarState(boolean isHidden)
	{
		mRoundProgressBar.setVisibility(isHidden ? View.INVISIBLE : View.VISIBLE);
	}
	
	private void playMovie(String videoPath) 
	{
		if (mMoviePlayer == null) mMoviePlayer = TuSDKMoviePlayer.createMoviePlayer();
		
		mMoviePlayer.setLooping(true);
		mMoviePlayer.initVideoPlayer(mActivity, Uri.fromFile(new File(mVideoPath)), mSurfaceView);
		mMoviePlayer.setDelegate(mMoviePlayerDelegate);
		mMoviePlayer.start();
	}
	
	public void pausePlayer()
	{
		if (mMoviePlayer != null) mMoviePlayer.pause();
	}
	
	public void resumePlayer()
	{
		if (mMoviePlayer != null) mMoviePlayer.resume();
	}
	
	public void stopPlayer()
	{
		if (mMoviePlayer != null) mMoviePlayer.stop();
	}
	
	public void videoSizeChanged(int width, int height)
	{
        final DisplayMetrics dm = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        final int screenWidth= (int) dm.widthPixels;
        final int screenHeight= (int) dm.heightPixels;
        Rect boundingRect = new Rect();
        boundingRect.left = 0;
        boundingRect.right = screenWidth;
        boundingRect.top = 0;
        boundingRect.bottom = screenHeight;
        Rect rect = RectHelper.makeRectWithAspectRatioInsideRect(new TuSdkSize(width, height), boundingRect);
        int w = rect.right- rect.left;
        int h = rect.bottom - rect.top;
        RelativeLayout.LayoutParams params = new RelativeLayout
                .LayoutParams(w,h);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mSurfaceView.setLayoutParams(params);	
	}

	
	/** 播放器委托 */
	private TuSDKMoviePlayerDelegate mMoviePlayerDelegate = new TuSDKMoviePlayerDelegate()
	{

		@Override
		public void onStateChanged(PlayerState state)
		{
		}

		@Override
		public void onVideSizeChanged(MediaPlayer mp,int width, int height) 
		{
			videoSizeChanged(width,height);
		}

		@Override
		public void onSeekComplete()
		{
		}

		@Override
		public void onProgress(int progress)
		{
		}

		@Override
		public void onCompletion()
		{
		}
	};
}
