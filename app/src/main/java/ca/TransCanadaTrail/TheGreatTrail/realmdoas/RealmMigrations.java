package ca.TransCanadaTrail.TheGreatTrail.realmdoas;

/**
 * Created by tarekAshraf on 8/3/17.
 */

import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

/**
 * Created by Ayman Mahgoub on 03/10/16
 */
public class RealmMigrations implements RealmMigration {

    public RealmMigrations() {
    }


    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema realmSchema = realm.getSchema();
        long version = oldVersion;

        if (oldVersion == 1) {
            realmSchema.get("Achievement")
                    .addField("seenAchievement", boolean.class);
            oldVersion++;
        }
    }

    private static class SchemaWrap {
        private RealmObjectSchema objectSchema;

        SchemaWrap(RealmObjectSchema objectSchema) {
            this.objectSchema = objectSchema;
        }

        SchemaWrap addField(String field, Class<?> clazz, FieldAttribute required) {
            try {
                this.objectSchema.addField(field, clazz, required);
            } catch (IllegalStateException ignored) {}
            return this;
        }

        SchemaWrap addField(String field, Class<?> clazz) {
            try {
                this.objectSchema.addField(field, clazz);
            } catch (IllegalStateException ignored) {}
            return this;
        }

        SchemaWrap addPrimaryKey(String field) {
            try {
                this.objectSchema.addPrimaryKey(field);
            } catch (IllegalStateException ignored) {}
            return this;
        }

        SchemaWrap addRealmListField(String field, RealmObjectSchema objectSchema) {
            try {
                this.objectSchema.addRealmListField(field, objectSchema);
            } catch (IllegalStateException ignored) {}
            return this;
        }

        SchemaWrap addRealmObjectField(String field, RealmObjectSchema objectSchema) {
            try {
                this.objectSchema.addRealmObjectField(field, objectSchema);
            } catch (IllegalStateException ignored) {}
            return this;
        }

        SchemaWrap transform(RealmObjectSchema.Function function) {
            this.objectSchema.transform(function);
            return this;
        }

        SchemaWrap removeField(String field) {
            try {
                this.objectSchema.removeField(field);
            } catch (IllegalStateException ignored) {}
            return this;
        }

        SchemaWrap setNullable(String field, boolean nullable) {
            try {
                this.objectSchema.setNullable(field, nullable);
            } catch (IllegalStateException ignored) {}
            return this;
        }

        SchemaWrap addIndex(String field) {
            try {
                this.objectSchema.removeField(field);
            } catch (IllegalStateException ignored) {}
            return this;
        }

        SchemaWrap renameField(String oldName, String newName) {
            try {
                this.objectSchema.renameField(oldName, newName);
            } catch (IllegalStateException ignored) {}
            return this;
        }
    }
}
