package notryken.chatnotify.config;

public class NotifyOption
{
    private String word;
    private int color;
    private boolean bold;
    private boolean italic;
    private boolean underlined;
    private boolean strikethrough;
    private boolean obfuscated;

    public NotifyOption(String word, int color, boolean bold, boolean italic,
                 boolean underlined, boolean strikethrough, boolean obfuscated)
    {
        this.word = word;
        this.color = color;
        this.bold = bold;
        this.italic = italic;
        this.underlined = underlined;
        this.strikethrough = strikethrough;
        this.obfuscated = obfuscated;
    }

    public void setWord(String word)
    {
        this.word = word;
    }

    public void setColor(int color)
    {
        this.color = color;
    }

    public void setBold(boolean bold)
    {
        this.bold = bold;
    }

    public void setItalic(boolean italic)
    {
        this.italic = italic;
    }

    public void setUnderlined(boolean underlined)
    {
        this.underlined = underlined;
    }

    public void setStrikethrough(boolean strikethrough)
    {
        this.strikethrough = strikethrough;
    }

    public void setObfuscated(boolean obfuscated)
    {
        this.obfuscated = obfuscated;
    }

    public String getWord()
    {
        return this.word;
    }

    public int getColor()
    {
        return this.color;
    }

    public boolean getBold()
    {
        return this.bold;
    }

    public boolean getItalic()
    {
        return this.italic;
    }

    public boolean getUnderlined()
    {
        return this.underlined;
    }

    public boolean getStrikethrough()
    {
        return this.strikethrough;
    }

    public boolean getObfuscated()
    {
        return this.obfuscated;
    }
}