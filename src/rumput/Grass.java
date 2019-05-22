/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rumput;

import java.io.Serializable;

/**
 *
 * @author harris046
 */
public class Grass implements Serializable{
    private int length;
    private boolean isLong;
    public static boolean cutting = false;
    
    public Grass(){
        length = 0;
        isLong = false;
    }
    
    /**
     * @return the length
     */
    public int getLength() {
        return length;
    }

    /**
     * @param length the length to set
     */
    public void setLength(int length) {
        this.length = length;
    }

    /**
     * @return the isLong
     */
    public boolean isIsLong() {
        return isLong;
    }

    /**
     * @param isLong the isLong to set
     */
    public void setIsLong(boolean isLong) {
        this.isLong = isLong;
    }
    
    /**
     * @return the cutting
     */
    public static boolean isCutting() {
        return cutting;
    }

    /**
     * @param aCutting the cutting to set
     */
    public static void setCutting(boolean aCutting) {
        cutting = aCutting;
    }
}
