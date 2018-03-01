package dalmora.henrique.movierecords;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import dalmora.henrique.movierecords.Interfaces.MarkWatchedListener;
import dalmora.henrique.movierecords.Interfaces.WatchedListener;

/**
 * Created by Henrique Dal' Mora R. da Silva on 30/10/2016.
 */

public class DialogWatched extends DialogFragment implements View.OnClickListener{
    private ImageButton mBtnClose;
    private Button mBtnWatched;

    private WatchedListener mListener;

    public DialogWatched(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_mark_as_watched, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBtnClose = (ImageButton) view.findViewById(R.id.btn_close);
        mBtnClose.setOnClickListener(this);
        mBtnWatched = (Button) view.findViewById(R.id.btn_watched);
        mBtnWatched.setOnClickListener(this);

        Bundle arguments = getArguments();
        if (arguments != null){
            int position = arguments.getInt("POSITION");
            Toast.makeText(getActivity(), "position: " + position, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch(id){
            case R.id.btn_watched:
                markAsWatched();
                break;
        }
        dismiss();
    }

    private void markAsWatched() {
        Bundle arguments = getArguments();
        if (mListener != null && arguments != null){
            int position = arguments.getInt("POSITION");
            mListener.onWatched(position);

        }
    }

    public void setCompleteListener(WatchedListener mWatchedListener) {
        mListener = mWatchedListener;
    }
}