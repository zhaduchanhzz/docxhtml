package com.viettel.signature.pdf;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.layout.properties.TextAlignment;

import java.util.Date;

public class DisplayConfig {
    public static final int SIGN_TYPE_CREATE_NEW_EMPTY_SIGNATURE_FIELD = 1;
    public static final int SIGN_TYPE_EXISTED_EMPTY_SIGNATURE_FIELD = 2;
    public String fieldName;
    public static final int DISPLAY_RECTANGLE_TEXT = 1;
    public static final int DISPLAY_IMAGE_STAMP = 2;
    public static final int DISPLAY_TABLE = 3;
    public static final int DISPLAY_IMAGE_WITH_TEXT = 4;
    public static final int DISPLAY_IMAGE_WITH_TEXT_VALID = 5;
    public static final boolean IS_DISPLAY_SIGNATURE_DEFAULT = true;
    public static final int TYPE_DISPLAY_DEFAULT = 1;
    public static final int SIGN_TYPE_DEFAULT = 1;
    private boolean isDisplaySignature = true;
    private int typeDisplay = 1;
    private int signType = 1;
    public static final int TOP_LEFT_LOCATION = 1;
    public static final int TOP_RIGHT_LOCATION = 2;
    public static final int BOTTOM_LEFT_LOCATION = 3;
    public static final int BOTTOM_RIGHT_LOCATION = 4;
    public static final int USER_DEFINE_LOCATION = 5;
    public static final int TOP_LEFT_LOCATION_CENTER = 6;
    public static final int LOCATE_SIGN_DEFAULT = 3;
    public static final int NUMBER_PAGE_SIGN_DEFAULT = 1;
    public static final float WIDTH_RECTANGLE_DEFAULT = 200.0F;
    public static final float HEIGHT_RECTANGLE_DEFAULT = 50.0F;
    public static final float MARGIN_TOP_OF_RECTANGLE_DEFAULT = 10.0F;
    public static final float MARGIN_BOTTOM_OF_RECTANGLE_DEFAULT = 10.0F;
    public static final float MARGIN_RIGHT_OF_RECTANGLE_DEFAULT = 10.0F;
    public static final float MARGIN_LEFT_OF_RECTANGLE_DEFAULT = 10.0F;
    private int locateSign = 3;
    private int numberPageSign = 1;
    private float widthRectangle = 200.0F;
    private float heightRectangle = 50.0F;
    private float marginTopOfRectangle = 10.0F;
    private float marginBottomOfRectangle = 10.0F;
    private float marginRightOfRectangle = 10.0F;
    private float marginLeftOfRectangle = 10.0F;
    public static final String SIGN_TEXT_FORMAT_1 = "Người Ký: %s";
    public static final String SIGN_TEXT_FORMAT_2 = "Người Ký: %s\r\nNgày Ký: %s";
    public static final String SIGN_TEXT_FORMAT_3 = "Người Ký: %s\r\nNgày Ký: %s\r\nLý Do: %s";
    public static final String SIGN_TEXT_FORMAT_4 = "Người Ký: %s\r\nNgày Ký: %s\r\nLý Do: %s\r\nĐịa Điểm: %s";
    public static final String SIGN_TEXT_FORMAT_5 = "Digital signed by: %s";
    public static final String SIGN_TEXT_FORMAT_6 = "Digital signed by: %s \r\nDate: %s";
    public static final String SIGN_TEXT_FORMAT_7 = "Digital signed by: %s \r\nDate: %s \r\nReason: %s";
    public static final String SIGN_TEXT_FORMAT_8 = "Digital signed by: %s \r\nDate: %s \r\nReason: %s \r\nLocation: %s";
    public static final String SIGN_TEXT_FORMAT_9 = "Người Ký: %s\r\n%sNgày ký: %s";
    public static final String SIGN_TEXT_FORMAT_10 = "Người Ký: %s\r\n%s\r\n%s\r\nNgày ký: %s";
    public static final String FORMAT_RECTANGLE_TEXT_DEFAULT = "Người Ký: %s\r\n%s\r\n%s\r\nNgày ký: %s";
    public static final String DATE_FORMAT_1 = "HH:mm:ss dd/MM/yyyy";
    public static final String DATE_FORMAT_2 = "yyyy/MM/dd HH:mm:ss";
    public static final String CONTACT_DEFAULT_EMPTY = "";
    public static final String REASON_DEFAULT_EMPTY = "";
    public static final String LOCATION_DEFAULT_EMPTY = "";
    public static final String ORGANIZATION_UNIT_DEFAULT_EMPTY = "";
    public static final String ORGANIZATION_DEFAULT_EMPTY = "";
    public static final String DISPLAY_TEXT_DEFAULT_EMPTY = "";
    public static final String DATE_FORMAT_STRING_DEFAULT = "HH:mm:ss dd/MM/yyyy";
    private String formatRectangleText = "Người Ký: %s\r\n%s\r\n%s\r\nNgày ký: %s";
    private String contact = "";
    private String reason = "";
    private String location = "";
    private String organizationUnit = "";
    private String organization = "";
    private String displayText = "";
    private Date signDate;
    private String dateFormatString = "HH:mm:ss dd/MM/yyyy";
    private String pathImage;
    public static final Rectangle PAGE_SIZE_DEFAULT;
    public static final int TOTAL_PAGE_SIGN_DEFAULT = 1;
    public static final int MAX_PAGE_SIGN_DEFAULT = 10;
    public static final float HEIGHT_TITLE_DEFAULT = 30.0F;
    public static final Color BACKGROUND_COLOR_TITLE_DEFAULT;
    public static final int SIZE_FONT_DEFAULT = 11;
    public static final float MARGIN_TOP_OF_TABLE_DEFAULT = 80.0F;
    public static final float MARGIN_BOTTOM_OF_TABLE_DEFAULT = 80.0F;
    public static final float MARGIN_RIGHT_OF_TABLE_DEFAULT = 60.0F;
    public static final boolean IS_DISPLAY_TITLE_PAGE_SIGN_DEFAULT = true;
    public static final String TITLE_PAGE_SIGN_DEFAULT = "TRANG KÝ";
    public static final float HEIGHT_ROW_TITLE_PAGE_SIGN_DEFAULT = 40.0F;
    public static final int FONT_SIZE_TITLE_PAGE_SIGN_DEFAULT = 15;
    public static final String FONT_PATH_TIMESNEWROMAN_WINDOWS = "C:/windows/fonts/times.ttf";
    public static final String FONT_PATH_TAHOMA_WINDOWS = "C:/windows/fonts/tahoma.ttf";
    public static final String FONT_PATH_ARIAL_WINDOWS = "C:/windows/fonts/arial.ttf";
    private Rectangle pageSize;
    private int totalPageSign;
    private int maxPageSign;
    private String[] titles;
    private float[] widthsPercen;
    private float heightTitle;
    private Color backgroundColorTitle;
    public static final String FONT_PATH_DEFAULT = "C:/windows/fonts/times.ttf";
    private String fontPath;
    private int sizeFont;
    private TextAlignment[] alignmentArray;
    private String[] textArray;
    private float marginTopOfTable;
    private float marginBottomOfTable;
    private float marginRightOfTable;
    private boolean isDisplayTitlePageSign;
    private String titlePageSign;
    private float heightRowTitlePageSign;
    private int fontSizeTitlePageSign;

    public DisplayConfig() {
        this.pageSize = PAGE_SIZE_DEFAULT;
        this.totalPageSign = 1;
        this.maxPageSign = 10;
        this.heightTitle = 30.0F;
        this.backgroundColorTitle = BACKGROUND_COLOR_TITLE_DEFAULT;
        this.fontPath = "C:/windows/fonts/times.ttf";
        this.sizeFont = 11;
        this.marginTopOfTable = 80.0F;
        this.marginBottomOfTable = 80.0F;
        this.marginRightOfTable = 60.0F;
        this.isDisplayTitlePageSign = true;
        this.titlePageSign = "TRANG KÝ";
        this.heightRowTitlePageSign = 40.0F;
        this.fontSizeTitlePageSign = 15;
    }

    public static DisplayConfig generateDisplayConfigNoDisplay(String contact, String reason, String location) {
        DisplayConfig config = new DisplayConfig();
        config.setIsDisplaySignature(false);
        config.setContact(contact);
        config.setReason(reason);
        config.setLocation(location);
        config.setSignDate(new Date());
        return config;
    }

    public static DisplayConfig generateDisplayConfigNoDisplay_ExistSignatureField(String contact, String reason, String location, String fieldName) {
        DisplayConfig config = generateDisplayConfigNoDisplay(contact, reason, location);
        config.setSignType(2);
        config.setFieldName(fieldName);
        return config;
    }

    public static DisplayConfig generateDisplayConfigTextDisplay_ExistSignatureField(String displayText, String contact, String reason, String location, String fontPath, int sizeFont, String fieldName) {
        DisplayConfig config = new DisplayConfig();
        config.setIsDisplaySignature(true);
        config.setTypeDisplay(1);
        config.setDisplayText(displayText);
        config.setContact(contact);
        config.setReason(reason);
        config.setLocation(location);
        config.setSignDate(new Date());
        config.setFontPath(fontPath);
        config.setSizeFont(sizeFont);
        config.setSignType(2);
        config.setFieldName(fieldName);
        return config;
    }

    public static DisplayConfig generateDisplayConfigRectangleText(int numberPageSign, float width, float height, int locateSign, float marginTopOfRectangle, float marginBottomOfRectangle, float marginRightOfRectangle, float marginLeftOfRectangle, String displayText, String formatRectangleText, String contact, String reason, String location, String dateFormatString, String fontPath, int sizeFont, String ou, String o) {
        DisplayConfig config = new DisplayConfig();
        config.setIsDisplaySignature(true);
        config.setTypeDisplay(1);
        config.setNumberPageSign(numberPageSign);
        config.setWidthRectangle(width);
        config.setHeightRectangle(height);
        config.setLocateSign(locateSign);
        config.setMarginTopOfRectangle(marginTopOfRectangle);
        config.setMarginBottomOfRectangle(marginBottomOfRectangle);
        config.setMarginRightOfRectangle(marginRightOfRectangle);
        config.setMarginLeftOfRectangle(marginLeftOfRectangle);
        config.setDisplayText(displayText);
        config.setFormatRectangleText(formatRectangleText);
        config.setContact(contact);
        config.setReason(reason);
        config.setOrganizationUnit(ou);
        config.setOrganization(o);
        config.setLocation(location);
        config.setDateFormatString(dateFormatString);
        config.setSignDate(new Date());
        config.setFontPath(fontPath);
        config.setSizeFont(sizeFont);
        return config;
    }

    public static DisplayConfig generateDisplayConfigRectangleText_ExistedSignatureField(String displayText, String formatRectangleText, String contact, String reason, String location, String dateFormatString, String fontPath, int sizeFont, String fieldName, String ou, String o) {
        DisplayConfig config = new DisplayConfig();
        config.setIsDisplaySignature(true);
        config.setTypeDisplay(1);
        config.setDisplayText(displayText);
        config.setFormatRectangleText(formatRectangleText);
        config.setContact(contact);
        config.setReason(reason);
        config.setOrganizationUnit(ou);
        config.setOrganization(o);
        config.setLocation(location);
        config.setDateFormatString(dateFormatString);
        config.setSignDate(new Date());
        config.setFontPath(fontPath);
        config.setSizeFont(sizeFont);
        config.setSignType(2);
        config.setFieldName(fieldName);
        return config;
    }

    public static DisplayConfig generateDisplayConfigImageText(int numberPageSign, float width, float height, int locateSign, float marginTopOfRectangle, float marginBottomOfRectangle, float marginRightOfRectangle, float marginLeftOfRectangle, String displayText, String formatRectangleText, String contact, String reason, String location, String dateFormatString, String fontPath, int sizeFont, String ou, String o, String pathImage) {
        DisplayConfig config = new DisplayConfig();
        config.setIsDisplaySignature(true);
        config.setTypeDisplay(4);
        config.setNumberPageSign(numberPageSign);
        config.setWidthRectangle(width);
        config.setHeightRectangle(height);
        config.setLocateSign(locateSign);
        config.setMarginTopOfRectangle(marginTopOfRectangle);
        config.setMarginBottomOfRectangle(marginBottomOfRectangle);
        config.setMarginRightOfRectangle(marginRightOfRectangle);
        config.setMarginLeftOfRectangle(marginLeftOfRectangle);
        config.setDisplayText(displayText);
        config.setFormatRectangleText(formatRectangleText);
        config.setContact(contact);
        config.setReason(reason);
        config.setOrganizationUnit(ou);
        config.setOrganization(o);
        config.setLocation(location);
        config.setDateFormatString(dateFormatString);
        config.setSignDate(new Date());
        config.setFontPath(fontPath);
        config.setSizeFont(sizeFont);
        config.setPathImage(pathImage);
        return config;
    }

    public static DisplayConfig generateDisplayConfigImageText_ExistedSignatureField(String displayText, String formatRectangleText, String contact, String reason, String location, String dateFormatString, String fontPath, int sizeFont, String fieldName, String ou, String o, String pathImage) {
        DisplayConfig config = new DisplayConfig();
        config.setIsDisplaySignature(true);
        config.setTypeDisplay(4);
        config.setDisplayText(displayText);
        config.setFormatRectangleText(formatRectangleText);
        config.setContact(contact);
        config.setReason(reason);
        config.setOrganizationUnit(ou);
        config.setOrganization(o);
        config.setLocation(location);
        config.setDateFormatString(dateFormatString);
        config.setSignDate(new Date());
        config.setFontPath(fontPath);
        config.setSizeFont(sizeFont);
        config.setSignType(2);
        config.setFieldName(fieldName);
        config.setPathImage(pathImage);
        return config;
    }

    public static DisplayConfig generateDisplayConfigImage(int numberPageSign, float width, float height, int locateSign, float marginTopOfRectangle, float marginBottomOfRectangle, float marginRightOfRectangle, float marginLeftOfRectangle, String contact, String reason, String location, String pathImage) {
        DisplayConfig config = new DisplayConfig();
        config.setIsDisplaySignature(true);
        config.setTypeDisplay(2);
        config.setNumberPageSign(numberPageSign);
        config.setWidthRectangle(width);
        config.setHeightRectangle(height);
        config.setLocateSign(locateSign);
        config.setMarginTopOfRectangle(marginTopOfRectangle);
        config.setMarginBottomOfRectangle(marginBottomOfRectangle);
        config.setMarginRightOfRectangle(marginRightOfRectangle);
        config.setMarginLeftOfRectangle(marginLeftOfRectangle);
        config.setContact(contact);
        config.setReason(reason);
        config.setLocation(location);
        config.setPathImage(pathImage);
        config.setSignDate(new Date());
        return config;
    }

    public static DisplayConfig generateDisplayConfigImage_ExistedSignatureField(String contact, String reason, String location, String pathImage, String fieldName) {
        DisplayConfig config = new DisplayConfig();
        config.setIsDisplaySignature(true);
        config.setTypeDisplay(2);
        config.setContact(contact);
        config.setReason(reason);
        config.setLocation(location);
        config.setPathImage(pathImage);
        config.setSignDate(new Date());
        config.setSignType(2);
        config.setFieldName(fieldName);
        return config;
    }

    public static DisplayConfig generateDisplayConfigTableDefault(int totalPageSign, String[] textArray, String fontPath) {
        DisplayConfig config = new DisplayConfig();
        config.setIsDisplaySignature(true);
        config.setTypeDisplay(3);
        config.setTotalPageSign(totalPageSign);
        config.titles = new String[5];
        config.titles[0] = "STT";
        config.titles[1] = "Người Ký";
        config.titles[2] = "Đơn vị";
        config.titles[3] = "Thời gian ký";
        config.titles[4] = "Ý kiến";
        config.widthsPercen = new float[5];
        config.widthsPercen[0] = 0.06F;
        config.widthsPercen[1] = 0.18F;
        config.widthsPercen[2] = 0.2F;
        config.widthsPercen[3] = 0.14F;
        config.widthsPercen[4] = 0.42F;
        config.alignmentArray = new TextAlignment[5];
        config.alignmentArray[0] = TextAlignment.CENTER;     // 1
        config.alignmentArray[1] = TextAlignment.LEFT;       // 0
        config.alignmentArray[2] = TextAlignment.JUSTIFIED;  // 3
        config.alignmentArray[3] = TextAlignment.LEFT;       // 0
        config.alignmentArray[4] = TextAlignment.JUSTIFIED;  // 3
        config.setTextArray(textArray);
        config.setSignDate(new Date());
        config.setFontPath(fontPath);
        return config;
    }

    public static DisplayConfig generateDisplayConfigTable(int totalPageSign, String[] titles, float[] widthsPercen, TextAlignment[] alignmentArray, String[] textArray, String fontPath) {
        DisplayConfig config = new DisplayConfig();
        config.setIsDisplaySignature(true);
        config.setTypeDisplay(3);
        config.setTotalPageSign(totalPageSign);
        config.setTitles(titles);
        config.setWidthsPercen(widthsPercen);
        config.setAlignmentArray(alignmentArray);
        config.setTextArray(textArray);
        config.setSignDate(new Date());
        config.setFontPath(fontPath);
        return config;
    }

    public boolean isIsDisplaySignature() {
        return this.isDisplaySignature;
    }

    public void setIsDisplaySignature(boolean isDisplaySignature) {
        this.isDisplaySignature = isDisplaySignature;
    }

    public int getTypeDisplay() {
        return this.typeDisplay;
    }

    public void setTypeDisplay(int typeDisplay) {
        this.typeDisplay = typeDisplay;
    }

    public int getSignType() {
        return this.signType;
    }

    public void setSignType(int signType) {
        this.signType = signType;
    }

    public int getNumberPageSign() {
        return this.numberPageSign;
    }

    public void setNumberPageSign(int numberPageSign) {
        this.numberPageSign = numberPageSign;
    }

    public float getWidthRectangle() {
        return this.widthRectangle;
    }

    public void setWidthRectangle(float widthRectangle) {
        this.widthRectangle = widthRectangle;
    }

    public float getHeightRectangle() {
        return this.heightRectangle;
    }

    public void setHeightRectangle(float heightRectangle) {
        this.heightRectangle = heightRectangle;
    }

    public String getFormatRectangleText() {
        return this.formatRectangleText;
    }

    public void setFormatRectangleText(String formatRectangleText) {
        this.formatRectangleText = formatRectangleText;
    }

    public String getContact() {
        return this.contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public Date getSignDate() {
        return this.signDate;
    }

    public void setSignDate(Date signDate) {
        this.signDate = signDate;
    }

    public String getDateFormatString() {
        return this.dateFormatString;
    }

    public void setDateFormatString(String dateFormatString) {
        this.dateFormatString = dateFormatString;
    }

    public String getReason() {
        return this.reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDisplayText() {
        return this.displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }

    public String getPathImage() {
        return this.pathImage;
    }

    public void setPathImage(String pathImage) {
        this.pathImage = pathImage;
    }

    public Rectangle getPageSize() {
        return this.pageSize;
    }

    public void setPageSize(Rectangle pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalPageSign() {
        return this.totalPageSign;
    }

    public void setTotalPageSign(int totalPageSign) {
        this.totalPageSign = totalPageSign;
    }

    public int getMaxPageSign() {
        return this.maxPageSign;
    }

    public void setMaxPageSign(int maxPageSign) {
        this.maxPageSign = maxPageSign;
    }

    public String[] getTitles() {
        return this.titles;
    }

    public void setTitles(String[] titles) {
        this.titles = titles;
    }

    public float[] getWidthsPercen() {
        return this.widthsPercen;
    }

    public void setWidthsPercen(float[] widthsPercen) {
        this.widthsPercen = widthsPercen;
    }

    public float getHeightTitle() {
        return this.heightTitle;
    }

    public void setHeightTitle(float heightTitle) {
        this.heightTitle = heightTitle;
    }

    public Color getBackgroundColorTitle() {
        return this.backgroundColorTitle;
    }

    public void setBackgroundColorTitle(Color backgroundColorTitle) {
        this.backgroundColorTitle = backgroundColorTitle;
    }

    public String getFontPath() {
        return this.fontPath;
    }

    public void setFontPath(String fontPath) {
        this.fontPath = fontPath;
    }

    public int getSizeFont() {
        return this.sizeFont;
    }

    public void setSizeFont(int sizeFont) {
        this.sizeFont = sizeFont;
    }

    public TextAlignment[] getAlignmentArray() {
        return this.alignmentArray;
    }

    public void setAlignmentArray(TextAlignment[] alignmentArray) {
        this.alignmentArray = alignmentArray;
    }

    public String[] getTextArray() {
        return this.textArray;
    }

    public void setTextArray(String[] textArray) {
        this.textArray = textArray;
    }

    public float getMarginTopOfTable() {
        return this.marginTopOfTable;
    }

    public void setMarginTopOfTable(float marginTopOfTable) {
        this.marginTopOfTable = marginTopOfTable;
    }

    public float getMarginBottomOfTable() {
        return this.marginBottomOfTable;
    }

    public void setMarginBottomOfTable(float marginBottomOfTable) {
        this.marginBottomOfTable = marginBottomOfTable;
    }

    public float getMarginRightOfTable() {
        return this.marginRightOfTable;
    }

    public void setMarginRightOfTable(float marginRightOfTable) {
        this.marginRightOfTable = marginRightOfTable;
    }

    public boolean isIsDisplayTitlePageSign() {
        return this.isDisplayTitlePageSign;
    }

    public void setIsDisplayTitlePageSign(boolean isDisplayTitlePageSign) {
        this.isDisplayTitlePageSign = isDisplayTitlePageSign;
    }

    public String getTitlePageSign() {
        return this.titlePageSign;
    }

    public void setTitlePageSign(String titlePageSign) {
        this.titlePageSign = titlePageSign;
    }

    public float getHeightRowTitlePageSign() {
        return this.heightRowTitlePageSign;
    }

    public void setHeightRowTitlePageSign(float heightRowTitlePageSign) {
        this.heightRowTitlePageSign = heightRowTitlePageSign;
    }

    public int getFontSizeTitlePageSign() {
        return this.fontSizeTitlePageSign;
    }

    public void setFontSizeTitlePageSign(int fontSizeTitlePageSign) {
        this.fontSizeTitlePageSign = fontSizeTitlePageSign;
    }

    public int getLocateSign() {
        return this.locateSign;
    }

    public void setLocateSign(int locateSign) {
        this.locateSign = locateSign;
    }

    public float getMarginTopOfRectangle() {
        return this.marginTopOfRectangle;
    }

    public void setMarginTopOfRectangle(float marginTopOfRectangle) {
        this.marginTopOfRectangle = marginTopOfRectangle;
    }

    public float getMarginBottomOfRectangle() {
        return this.marginBottomOfRectangle;
    }

    public void setMarginBottomOfRectangle(float marginBottomOfRectangle) {
        this.marginBottomOfRectangle = marginBottomOfRectangle;
    }

    public float getMarginRightOfRectangle() {
        return this.marginRightOfRectangle;
    }

    public void setMarginRightOfRectangle(float marginRightOfRectangle) {
        this.marginRightOfRectangle = marginRightOfRectangle;
    }

    public float getMarginLeftOfRectangle() {
        return this.marginLeftOfRectangle;
    }

    public void setMarginLeftOfRectangle(float marginLeftOfRectangle) {
        this.marginLeftOfRectangle = marginLeftOfRectangle;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getOrganizationUnit() {
        return this.organizationUnit;
    }

    public void setOrganizationUnit(String organizationUnit) {
        this.organizationUnit = organizationUnit;
    }

    public String getOrganization() {
        return this.organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    static {
        PAGE_SIZE_DEFAULT = PageSize.A4;
        BACKGROUND_COLOR_TITLE_DEFAULT = new DeviceRgb(240, 240, 240);
    }
}
