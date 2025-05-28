package xyz.openexonaut.extension.exolib.messages;

import java.util.*;

import com.smartfoxserver.v2.entities.*;
import com.smartfoxserver.v2.entities.variables.*;

public class ExoVariableUpdate {
    public final User user;
    public final List<UserVariable> variableList;

    public ExoVariableUpdate(User user, List<UserVariable> variableList) {
        this.user = user;
        this.variableList = variableList;
    }
}
