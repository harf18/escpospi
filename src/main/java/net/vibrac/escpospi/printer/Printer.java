package net.vibrac.escpospi.printer;

import net.vibrac.escpospi.exception.BarcodeSizeError;
import net.vibrac.escpospi.bus.BusConnexion;
import net.vibrac.escpospi.bus.BusFactory;
import net.vibrac.escpospi.exception.ConnectionException;
import net.vibrac.escpospi.exception.QRCodeException;
import net.vibrac.escpospi.image.Image;
import net.vibrac.escpospi.qrcode.QRCodeGenerator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class Printer {

    // Feed control sequences
    private static final byte[] CTL_LF          = {0x0a};          // Print and line feed
    // Line Spacing
    private static final byte[] LINE_SPACE_24   = {0x1b,0x33,24}; // Set the line spacing at 24
    private static final byte[] LINE_SPACE_30   = {0x1b,0x33,30}; // Set the line spacing at 30
    //Image
    private static final byte[] SELECT_BIT_IMAGE_MODE = {0x1B, 0x2A, 33};
    // Printer hardware
    private static final byte[] HW_INIT         = {0x1b,0x40};          // Clear data in buffer and reset modes
    // Cash Drawer
    private static final byte[] CD_KICK_2       = {0x1b,0x70,0x00};      // Sends a pulse to pin 2 []
    private static final byte[] CD_KICK_5       = {0x1b,0x70,0x01};      // Sends a pulse to pin 5 []
    // Paper
    private static final byte[]  PAPER_FULL_CUT = {0x1d,0x56,0x00}; // Full cut paper
    private static final byte[]  PAPER_PART_CUT = {0x1d,0x56,0x01}; // Partial cut paper
    // Text format
    private static final byte[] TXT_NORMAL      = {0x1b,0x21,0x00}; // Normal text
    private static final byte[] TXT_2HEIGHT     = {0x1b,0x21,0x10}; // Double height text
    private static final byte[] TXT_2WIDTH      = {0x1b,0x21,0x20}; // Double width text
    private static final byte[] TXT_4SQUARE     = {0x1b,0x21,0x30}; // Quad area text
    private static final byte[] TXT_UNDERL_OFF  = {0x1b,0x2d,0x00}; // Underline font OFF
    private static final byte[] TXT_UNDERL_ON   = {0x1b,0x2d,0x01}; // Underline font 1-dot ON
    private static final byte[] TXT_UNDERL2_ON  = {0x1b,0x2d,0x02}; // Underline font 2-dot ON
    private static final byte[] TXT_BOLD_OFF    = {0x1b,0x45,0x00}; // Bold font OFF
    private static final byte[] TXT_BOLD_ON     = {0x1b,0x45,0x01}; // Bold font ON
    private static final byte[] TXT_FONT_A      = {0x1b,0x4d,0x00}; // Font type A
    private static final byte[] TXT_FONT_B      = {0x1b,0x4d,0x01};// Font type B
    private static final byte[] TXT_ALIGN_LT    = {0x1b,0x61,0x00}; // Left justification
    private static final byte[] TXT_ALIGN_CT    = {0x1b,0x61,0x01}; // Centering
    private static final byte[] TXT_ALIGN_RT    = {0x1b,0x61,0x02}; // Right justification
    // Char code table
    private static final byte[] CHARCODE_PC437  = {0x1b,0x74,0x00}; // USA){ Standard Europe
    private static final byte[] CHARCODE_JIS    = {0x1b,0x74,0x01}; // Japanese Katakana
    private static final byte[] CHARCODE_PC850  = {0x1b,0x74,0x02}; // Multilingual
    private static final byte[] CHARCODE_PC860  = {0x1b,0x74,0x03}; // Portuguese
    private static final byte[] CHARCODE_PC863  = {0x1b,0x74,0x04}; // Canadian-French
    private static final byte[] CHARCODE_PC865  = {0x1b,0x74,0x05}; // Nordic
    private static final byte[] CHARCODE_WEU    = {0x1b,0x74,0x06}; // Simplified Kanji, Hirakana
    private static final byte[] CHARCODE_GREEK  = {0x1b,0x74,0x07}; // Simplified Kanji
    private static final byte[] CHARCODE_HEBREW = {0x1b,0x74,0x08}; // Simplified Kanji
    private static final byte[] CHARCODE_PC1252 = {0x1b,0x74,0x10}; // Western European Windows Code Set
    private static final byte[] CHARCODE_PC866  = {0x1b,0x74,0x12}; // Cirillic //2
    private static final byte[] CHARCODE_PC852  = {0x1b,0x74,0x13}; // Latin 2
    private static final byte[] CHARCODE_PC858  = {0x1b,0x74,0x14}; // Euro
    private static final byte[] CHARCODE_THAI42 = {0x1b,0x74,0x15}; // Thai character code 42
    private static final byte[] CHARCODE_THAI11 = {0x1b,0x74,0x16}; // Thai character code 11
    private static final byte[] CHARCODE_THAI13 = {0x1b,0x74,0x17}; // Thai character code 13
    private static final byte[] CHARCODE_THAI14 = {0x1b,0x74,0x18}; // Thai character code 14
    private static final byte[] CHARCODE_THAI16 = {0x1b,0x74,0x19}; // Thai character code 16
    private static final byte[] CHARCODE_THAI17 = {0x1b,0x74,0x1a}; // Thai character code 17
    private static final byte[] CHARCODE_THAI18 = {0x1b,0x74,0x1b}; // Thai character code 18

    // Barcode format
    private static final byte[] BARCODE_TXT_OFF = {0x1d,0x48,0x00}; // HRI printBarcode chars OFF
    private static final byte[] BARCODE_TXT_ABV = {0x1d,0x48,0x01}; // HRI printBarcode chars above
    private static final byte[] BARCODE_TXT_BLW = {0x1d,0x48,0x02}; // HRI printBarcode chars below
    private static final byte[] BARCODE_TXT_BTH = {0x1d,0x48,0x03}; // HRI printBarcode chars both above and below
    private static final byte[] BARCODE_FONT_A  = {0x1d,0x66,0x00}; // Font type A for HRI printBarcode chars
    private static final byte[] BARCODE_FONT_B  = {0x1d,0x66,0x01}; // Font type B for HRI printBarcode chars
    private static final byte[] BARCODE_HEIGHT  = {0x1d,0x68,0x64}; // Barcode Height [1-255]
    private static final byte[] BARCODE_WIDTH   = {0x1d,0x77,0x03}; // Barcode Width  [2-6]
    private static final byte[] BARCODE_UPC_A   = {0x1d,0x6b,0x00}; // Barcode type UPC-A
    private static final byte[] BARCODE_UPC_E   = {0x1d,0x6b,0x01}; // Barcode type UPC-E
    private static final byte[] BARCODE_EAN13   = {0x1d,0x6b,0x02}; // Barcode type EAN13
    private static final byte[] BARCODE_EAN8    = {0x1d,0x6b,0x03}; // Barcode type EAN8
    private static final byte[] BARCODE_CODE39  = {0x1d,0x6b,0x04}; // Barcode type CODE39
    private static final byte[] BARCODE_ITF     = {0x1d,0x6b,0x05}; // Barcode type ITF
    private static final byte[] BARCODE_NW7     = {0x1d,0x6b,0x06}; // Barcode type NW7
    // Printing Density
    private static final byte[] PD_N50          = {0x1d,0x7c,0x00}; // Printing Density -50%
    private static final byte[] PD_N37          = {0x1d,0x7c,0x01}; // Printing Density -37.5%
    private static final byte[] PD_N25          = {0x1d,0x7c,0x02}; // Printing Density -25%
    private static final byte[] PD_N12          = {0x1d,0x7c,0x03}; // Printing Density -12.5%
    private static final byte[] PD_0            = {0x1d,0x7c,0x04}; // Printing Density  0%
    private static final byte[] PD_P50          = {0x1d,0x7c,0x08}; // Printing Density +50%
    private static final byte[] PD_P37          = {0x1d,0x7c,0x07}; // Printing Density +37.5%
    private static final byte[] PD_P25          = {0x1d,0x7c,0x06}; // Printing Density +25%
    private static final byte[] PD_P12          = {0x1d,0x7c,0x05}; // Printing Density +12.5%

    private final BusConnexion bus;
    
    public Printer(String busType){
        bus = BusFactory.getBus(busType);
    }


    public void print(String text) throws ConnectionException {
        try {
            bus.write(text.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new ConnectionException("Unable to print text", e);
        }
    }

    public void printLn(String text) throws ConnectionException {
        print(text + "\n");
    }

    public void lineBreak(){
        lineBreak(1);
    }

    public void lineBreak(int nbLine){
        for (int i=0;i<nbLine;i++) {
            bus.write(CTL_LF);
        }
    }

    public void printQRCode(String value, int size) throws QRCodeException {
        QRCodeGenerator q = new QRCodeGenerator();
        printImage(q.generate(value, size));
    }

    public void printQRCode(String value) throws QRCodeException {
        printQRCode(value, 150);
    }

    public void setTextSizeNormal(){
        setTextSize(1,1);
    }

    public void setTextSize2H(){
        setTextSize(1,2);
    }

    public void setTextSize2W(){
        setTextSize(2,1);
    }

    public void setText4Square(){
        setTextSize(2,2);
    }

    private void setTextSize(int width, int height){
        if (height == 2 && width == 2) {
            bus.write(TXT_NORMAL);
            bus.write(TXT_4SQUARE);
        }else if(height == 2) {
            bus.write(TXT_NORMAL);
            bus.write(TXT_2HEIGHT);
        }else if(width == 2){
            bus.write(TXT_NORMAL);
            bus.write(TXT_2WIDTH);
        }else{
            bus.write(TXT_NORMAL);
        }
    }

    public void setTextTypeBold(){
        setTextType("B");
    }

    public void setTextTypeUnderline(){
        setTextType("U");
    }

    public void setTextType2Underline(){
        setTextType("U2");
    }

    public void setTextTypeBold2Underline(){
        setTextType("BU2");
    }

    public void setTextTypeNormal(){
        setTextType("NORMAL");
    }

    private void setTextType(String type){
        if (type.equalsIgnoreCase("B")){
            bus.write(TXT_BOLD_ON);
            bus.write(TXT_UNDERL_OFF);
        }else if(type.equalsIgnoreCase("U")){
            bus.write(TXT_BOLD_OFF);
            bus.write(TXT_UNDERL_ON);
        }else if(type.equalsIgnoreCase("U2")){
            bus.write(TXT_BOLD_OFF);
            bus.write(TXT_UNDERL2_ON);
        }else if(type.equalsIgnoreCase("BU")){
            bus.write(TXT_BOLD_ON);
            bus.write(TXT_UNDERL_ON);
        }else if(type.equalsIgnoreCase("BU2")){
            bus.write(TXT_BOLD_ON);
            bus.write(TXT_UNDERL2_ON);
        }else if(type.equalsIgnoreCase("NORMAL")){
            bus.write(TXT_BOLD_OFF);
            bus.write(TXT_UNDERL_OFF);
        }
    }

    public void cutPart(){
        cut("PART");
    }

    public void cutFull(){
        cut("FULL");
    }

    private void cut(String mode){
        for (int i=0;i<6;i++){
            bus.write(CTL_LF);
        }
        if (mode.toUpperCase().equals("PART")){
            bus.write(PAPER_PART_CUT);
        }else{
            bus.write(PAPER_FULL_CUT);
        }
    }

    public void printBarcode(String code, String bc, int width, int height, String pos, String font) throws BarcodeSizeError {
     // Align Bar Code()
    bus.write(TXT_ALIGN_CT);
    // Height
    if (height >=2 || height <=6) {
        bus.write(BARCODE_HEIGHT);
    }else {
        throw new BarcodeSizeError("Incorrect Height");
    }
    //Width
    if (width >= 1 || width <=255) {
        bus.write(BARCODE_WIDTH);
    }else {
        throw new BarcodeSizeError("Incorrect Width");
    }
    //Font
    if (font.equalsIgnoreCase("B")) {
        bus.write(BARCODE_FONT_B);
    }else {
        bus.write(BARCODE_FONT_A);
    }
    //Position
    if (pos.equalsIgnoreCase("OFF")) {
        bus.write(BARCODE_TXT_OFF);
    }else if (pos.equalsIgnoreCase("BOTH")) {
        bus.write(BARCODE_TXT_BTH);
    }else if (pos.equalsIgnoreCase("ABOVE")) {
        bus.write(BARCODE_TXT_ABV);
    }else {
        bus.write(BARCODE_TXT_BLW);
    }
    //Type
    switch(bc.toUpperCase()){
        case "UPC-A":
            bus.write(BARCODE_UPC_A);
            break;
        case "UPC-E":
            bus.write(BARCODE_UPC_E);
            break;
        default: case "EAN13":
            bus.write(BARCODE_EAN13);
            break;
        case "EAN8":
            bus.write(BARCODE_EAN8);
            break;
        case "CODE39":
            bus.write(BARCODE_CODE39);
            break;
        case "ITF":
            bus.write(BARCODE_ITF);
            break;
        case "NW7":
            bus.write(BARCODE_NW7);
            break;
        }
        //Print Code
        if (!code.equals("")) {
            bus.write(code.getBytes());
            bus.write(CTL_LF);
        } else {
            throw new BarcodeSizeError("Incorrect Value");
        }
    }

    public void setTextFontA(){
        setTextFont("A");
    }

    public void setTextFontB(){
        setTextFont("B");
    }

    private void setTextFont(String font){
        if (font.equalsIgnoreCase("B")){
            bus.write(TXT_FONT_B);
        }else{
            bus.write(TXT_FONT_A);
        }
    }

    public void setTextAlignCenter(){
        setTextAlign("CENTER");
    }

    public void setTextAlignRight(){
        setTextAlign("RIGHT");
    }

    public void setTextAlignLeft(){
        setTextAlign("LEFT");
    }

    private void setTextAlign(String align){
        if (align.equalsIgnoreCase("CENTER")){
            bus.write(TXT_ALIGN_CT);
        }else if( align.equalsIgnoreCase("RIGHT")){
            bus.write(TXT_ALIGN_RT);
        }else{
            bus.write(TXT_ALIGN_LT);
        }
    }

    public void setTextDensity(int density){
        switch (density){
            case 0:
                bus.write(PD_N50);
                break;
            case 1:
                bus.write(PD_N37);
                break;
            case 2:
                bus.write(PD_N25);
                break;
            case 3:
                bus.write(PD_N12);
                break;
            case 4:
                bus.write(PD_0);
                break;
            case 5:
                bus.write(PD_P12);
                break;
            case 6:
                bus.write(PD_P25);
                break;
            case 7:
                bus.write(PD_P37);
                break;
            case 8:
                bus.write(PD_P50);
                break;
        }
    }

    public void setTextNormal(){
        setTextProperties("LEFT", "A", "NORMAL", 1,1,9);
    }

    public void setTextProperties(String align, String font, String type, int width, int height, int density){
        setTextAlign(align);
        setTextFont(font);
        setTextType(type);
        setTextSize(width, height);
        setTextDensity(density);
    }


    public void printImage(String filePath) throws IOException {
        File img = new File(filePath);
        printImage(ImageIO.read(img));
    }


    private void printImage(BufferedImage image) {
        Image img = new Image();
        int[][] pixels = img.getPixelsSlow(image);
        for (int y = 0; y < pixels.length; y += 24) {
            bus.write(LINE_SPACE_24);
            bus.write(SELECT_BIT_IMAGE_MODE);
            bus.write(new byte[]{(byte)(0x00ff & pixels[y].length), (byte)((0xff00 & pixels[y].length) >> 8)});
            for (int x = 0; x < pixels[y].length; x++) {
                bus.write(img.recollectSlice(y, x, pixels));
            }

            bus.write(CTL_LF);
        }
        bus.write(CTL_LF);
        bus.write(LINE_SPACE_30);
    }

    public void setCharCode(String code)  {
        switch (code){
            case "USA":
                bus.write(CHARCODE_PC437);
                break;
            case "JIS":
                bus.write(CHARCODE_JIS);
                break;
            case "MULTILINGUAL":
                bus.write(CHARCODE_PC850);
                break;
            case "PORTUGUESE":
                bus.write(CHARCODE_PC860);
                break;
            case "CA_FRENCH":
                bus.write(CHARCODE_PC863);
                break;
            default: case "NORDIC":
                bus.write(CHARCODE_PC865);
                break;
            case "WEST_EUROPE":
                bus.write(CHARCODE_WEU);
                break;
            case "GREEK":
                bus.write(CHARCODE_GREEK);
                break;
            case "HEBREW":
                bus.write(CHARCODE_HEBREW);
                break;
            case "WPC1252":
                bus.write(CHARCODE_PC1252);
                break;
            case "CIRILLIC2":
                bus.write(CHARCODE_PC866);
                break;
            case "LATIN2":
                bus.write(CHARCODE_PC852);
                break;
            case "EURO":
                bus.write(CHARCODE_PC858);
                break;
            case "THAI42":
                bus.write(CHARCODE_THAI42);
                break;
            case "THAI11":
                bus.write(CHARCODE_THAI11);
                break;
            case "THAI13":
                bus.write(CHARCODE_THAI13);
                break;
            case "THAI14":
                bus.write(CHARCODE_THAI14);
                break;
            case "THAI16":
                bus.write(CHARCODE_THAI16);
                break;
            case "THAI17":
                bus.write(CHARCODE_THAI17);
                break;
            case "THAI18":
                bus.write(CHARCODE_THAI18);
                break;
        }
    }

    public void init(){
        bus.write(HW_INIT);
    }

    public void openCashDrawerPin2() {
        bus.write(CD_KICK_2);
    }

    public void openCashDrawerPin5() {
        bus.write(CD_KICK_5);
    }

    public void open(String address, int baudRate){
        bus.open(address, baudRate);
    }

    public void close(){
        bus.close();
    }
}
