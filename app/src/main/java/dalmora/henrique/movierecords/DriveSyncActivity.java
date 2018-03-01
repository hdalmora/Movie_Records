package dalmora.henrique.movierecords;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.MetadataChangeSet;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import dalmora.henrique.movierecords.data.MovieDbHelper;

/**
 * Created by hdalmora on 24/10/2016.
 */

public class DriveSyncActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String LOG_TAG = DriveSyncActivity.class.getSimpleName();

    private static final int REQUEST_CODE_RESOLUTION = 3;
    private static final int REQUEST_CODE_CREATOR = 2;
    private GoogleApiClient mGoogleApiClient;

    DriveId mDriveId;


    /******************************************************************
     * create file in GOODrive
     * @param pFldr parent's ID
     * @param titl  file name
     * @param mime  file mime type  (application/x-sqlite3)
     * @param file  file (with content) to create
     */
    void saveToDrive(final DriveFolder pFldr, final String titl,
                     final String mime, final java.io.File file) {
        if (mGoogleApiClient != null && pFldr != null && titl != null && mime != null && file != null) try {
            // create content from file
            Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
                    DriveContents cont = driveContentsResult != null && driveContentsResult.getStatus().isSuccess() ?
                            driveContentsResult.getDriveContents() : null;

                    // write file to content, chunk by chunk
                    if (cont != null) try {
                        OutputStream oos = cont.getOutputStream();
                        if (oos != null) try {
                            InputStream is = new FileInputStream(file);
                            byte[] buf = new byte[4096];
                            int c;
                            while ((c = is.read(buf, 0, buf.length)) > 0) {
                                oos.write(buf, 0, c);
                                oos.flush();
                            }
                        }
                        finally { oos.close();}

                        // content's COOL, create metadata
                        MetadataChangeSet meta = new MetadataChangeSet.Builder().setTitle(titl).setMimeType(mime).build();

                        // now create file on GooDrive
                        pFldr.createFile(mGoogleApiClient, meta, cont).setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                            @Override
                            public void onResult(DriveFolder.DriveFileResult driveFileResult) {
                                if (driveFileResult != null && driveFileResult.getStatus().isSuccess()) {
                                    DriveFile dFil = driveFileResult != null && driveFileResult.getStatus().isSuccess() ?
                                            driveFileResult.getDriveFile() : null;
                                    if (dFil != null) {
                                        // BINGO , file uploaded
                                        dFil.getMetadata(mGoogleApiClient).setResultCallback(new ResultCallback<DriveResource.MetadataResult>() {
                                            @Override
                                            public void onResult(DriveResource.MetadataResult metadataResult) {
                                                if (metadataResult != null && metadataResult.getStatus().isSuccess()) {
                                                    mDriveId = metadataResult.getMetadata().getDriveId();
                                                }
                                            }
                                        });
                                    }
                                } else {
                                    /* report error */
                                    Toast.makeText(DriveSyncActivity.this, "ERRO SYNC DRIVE", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } catch (Exception e) { e.printStackTrace(); }
                }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    /*******************************************************************
     * get file contents
     */
    void readFromGooDrive() {
        byte[] buf = null;
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) try {
            DriveFile df = Drive.DriveApi.getFile(mGoogleApiClient, mDriveId);
            df.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)
                    .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                        @Override
                        public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
                            if ((driveContentsResult != null) && driveContentsResult.getStatus().isSuccess()) {
                                DriveContents cont = driveContentsResult.getDriveContents();
                                // DUMP cont.getInputStream() to your DB file
                                cont.discard(mGoogleApiClient);    // or cont.commit();  they are equiv if READONLY
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mGoogleApiClient == null) {
            // Create the API client and bind it to an instance variable.
            // We use this instance as the callback for connection and connection
            // failures.
            // Since no account name is passed, the user is prompted to choose.
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        // Connect the client. Once connected, the camera is launched.
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(LOG_TAG, "API client connected.");

        // Testing to see if database is indeed uploaded to google drive app folder
        String db_name = MovieDbHelper.DATABASE_NAME;
        String currentDBPath = this.getDatabasePath(db_name).toString();
        Log.d(LOG_TAG, "DB PATH: " + currentDBPath);
        saveToDrive(
                Drive.DriveApi.getRootFolder(mGoogleApiClient),
                db_name,
                "application/x-sqlite3",
                new java.io.File(currentDBPath)
        );


    }


    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(LOG_TAG, "GoogleApiClient connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Called whenever the API client fails to connect.
        Log.i(LOG_TAG, "GoogleApiClient connection failed: " + connectionResult.toString());
        if (!connectionResult.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, connectionResult.getErrorCode(), 0).show();
            return;
        }

        // The failure has a resolution. Resolve it.
        // Called typically when the app is not yet authorized, and an
        // authorization
        // dialog is displayed to the user.
        try {
            connectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(LOG_TAG, "Exception while starting resolution activity", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_RESOLUTION:
                if (resultCode == RESULT_OK) {
                    mGoogleApiClient.connect();
                }
                break;
            case REQUEST_CODE_CREATOR:
                if (resultCode == Activity.RESULT_OK) {
                   /* mBitmapToSave = null;
                    // Just start the camera again for another photo.
                    startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE),
                            REQUEST_CODE_CAPTURE_IMAGE);*/
                } else {
                    // User denied access, show him the account chooser again
                }
                break;
        }
    }


}
