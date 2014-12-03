package tk.aegisstudios.traveltracker;

import android.widget.EditText;

public final class FieldUtil {
	
	int attemptedInstantiation;
	
	// Static class simulation
	private FieldUtil() {
		this.attemptedInstantiation = 1;
	}
	
    public static boolean fieldsEqual(EditText field, EditText field2) {
        String fieldText = field.getText().toString();
        String fieldText2 = field2.getText().toString();
    	return fieldText.equals(fieldText2);
    }

    public static boolean fieldIsBlank(EditText field) {
        return field.getText().toString().equals("");    
    }
    
}
