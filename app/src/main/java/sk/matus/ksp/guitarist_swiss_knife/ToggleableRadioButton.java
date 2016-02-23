package sk.matus.ksp.guitarist_swiss_knife;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;

/**
 * A custom RadioButton that can be unchecked (even within a RadioGroup component)
 */
public class ToggleableRadioButton extends RadioButton {
    ArrayList<Dependency>dependencies = new ArrayList<>();
    public ToggleableRadioButton(Context context){
        super(context);
    }
    public ToggleableRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ToggleableRadioButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * This method takes care of the case when the ToggleableRadioButton is a child to a RadioGroup.
     * Since RadioGroup only allows one of its children to be checked, unchecking this button
     * equals the action of clearing the entire group.
     */
    @Override
    public void toggle() {
        if(isChecked()) {
            if(getParent() instanceof RadioGroup) {
                ((RadioGroup)getParent()).clearCheck();
            }
        } else {
            setChecked(true);
        }
    }

    public ArrayList<Dependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(ArrayList<Dependency> dependencies) {
        this.dependencies = dependencies;
    }

    public void addDependency(Dependency dependency){
        dependencies.add(dependency);
    }
}