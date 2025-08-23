package net.forestany.mediacollectionrest.webservice;

public class JSONMediaCollection {

    public java.time.LocalDateTime Timestamp = null;
    public java.util.List<LanguageRecord> Languages = new java.util.ArrayList<>();
    public java.util.List<MediaCollectionRecord> Records = new java.util.ArrayList<>();

    public JSONMediaCollection() {

    }

    @Override public String toString() {
        String s_foo = "";

        for (java.lang.reflect.Field o_field : this.getClass().getDeclaredFields()) {
            if (o_field.getName().startsWith("this$")) {
                continue;
            }

            try {
                if (o_field.get(this) instanceof java.util.List<?>) {
                    @SuppressWarnings("unchecked")
                    java.util.List<Object> a_items = (java.util.List<Object>)o_field.get(this);

                    s_foo += o_field.getName() + " = [";

                    if (a_items.size() > 0) {
                        for (Object o_foo : a_items) {
                            s_foo += o_foo.toString() + ",";
                        }

                        s_foo = s_foo.substring(0, s_foo.length() - 1);
                    }

                    s_foo += "]|";
                } else {
                    s_foo += o_field.getName() + " = " + o_field.get(this).toString() + "|";
                }
            } catch (Exception o_exc) {
                s_foo += o_field.getName() + " = null|";
            }
        }

        s_foo = s_foo.substring(0, s_foo.length() - 1);

        return s_foo;
    }
}
