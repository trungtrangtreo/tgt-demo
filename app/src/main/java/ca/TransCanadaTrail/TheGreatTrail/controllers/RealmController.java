package ca.TransCanadaTrail.TheGreatTrail.controllers;

import android.content.Context;

import ca.TransCanadaTrail.TheGreatTrail.realmdoas.RealmMigrations;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmError;
import io.realm.exceptions.RealmMigrationNeededException;

/*

 Created by Ayman Mahgoub on 03/10/16

*/

public class RealmController {

    public static final String TAG = RealmController.class.getName();

    public static final int SCHEMA_VERSION = 1;
    public static final String DATABASE_NAME = "great-trail.realm";

    public static void configureDb(Context context) {
        Realm.init(context);
        Realm.setDefaultConfiguration(getConfiguration());
    }

    public static void checkDatabaseMigrations(Context context) {
        try {
            Realm.init(context);
            Realm.setDefaultConfiguration(getConfiguration());
            Realm realm = Realm.getDefaultInstance();
            realm.close();
        } catch (RealmMigrationNeededException exception) {
            deleteRealmDatabase(getConfiguration());
        } catch (IllegalArgumentException illegalArgumentException) {
            deleteRealmDatabase(getConfiguration());
        } catch (RealmError realmError) {
            deleteRealmDatabase(getConfiguration());
        }
    }

    private static RealmConfiguration getConfiguration() {
        return new RealmConfiguration.Builder()
                .name(DATABASE_NAME)
                .schemaVersion(SCHEMA_VERSION)
                .migration(new RealmMigrations())
                .build();
    }

    private static void deleteRealmDatabase(RealmConfiguration realmConfiguration) {
        Realm.deleteRealm(realmConfiguration); //drop database if it needs migration
    }
}

