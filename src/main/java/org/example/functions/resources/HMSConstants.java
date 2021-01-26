package org.example.functions.resources;

public interface HMSConstants {
    public static final String NULLVALUE = "null";
    public static final String NEXTLINE = "\n";
    public static final String EMPTYSTRING = "";
    public static final String LEFTBRACE = "(";
    public static final String RIGHTBRACE = "(";
    public static final String AND = " and ";
    public static final String EQ = " eq ";
    public static final String SINGLEQUOTE = "'";
    public static final String COLON = " : ";
    public static final String COMMA = ",";

    public static final String  AGE = "Age";
    public static final String  NAME = "Name";
    public static final String  PERSONTYPE = "Person Type";

    // Query Params for Search
    public static final String AGGREGATIONFIELD = "aggregationField";
    public static final String AGGREGATIONFIELDVALUES = "aggregationFieldValues";
    public static final String LIMIT = "limit";
    public static final String OFFSET = "offset";
    public static final String KEYWORD = "keyword";
    public static final String TOP = "top";
    public static final String CODE = "code";

    public static final String DECIMAL_STRING_PATTERN = "([0-9]*)\\.([0-9]*)";
    public static final String STATE_INIT = "init";
    public static final String YES = "Yes";
    public static final String NO = "No";

    // Actions
    public static final String ACTION_EDIT = "Edit";
    public static final String ACTION_CREATE = "Create";

    public static final String DATEFORMAT_DEFAULT = "MM-dd-yyyy";

    // Errors
    public static final String ERROR_PERSON_NOT_FOUND = "person not found.";
	public static final String MANDATORY_PARAMS_ERROR = "Mandatory params are missing, ";
    public static final String EXP_TIME_INTERVAL = "30";
    public static final String TIME_STAMP = "timeStamp";

}
