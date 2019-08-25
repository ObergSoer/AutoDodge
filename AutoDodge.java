    
    import ch.aplu.jgamegrid.*;
    import ch.aplu.util.LoResTimer;
    import java.awt.event.KeyEvent;
    import java.awt.*;
    import ch.aplu.jgamegrid.GGBackground;
    
    public class AutoDodge extends GameGrid
    {
        // Timer zur berechnug des Punktestandes und der Unverwundbarkeitszeit nach einem Treffer
        public LoResTimer gametime;
        long lastUpdateTime = 0;
        long lastDeathTime = 0;
    
        public int nitro = 300;
        private GGProgressBar nitrobar;
    
        public boolean gameover = false;
        private GameoverAnzeige gameoveranzeige;
    
        private Auto player;
    
        public int leben = 3;
        private LebensAnzeige lebensanzeige;
    
        private ZifferAnzeige einer;
        private ZifferAnzeige zehner;
        private ZifferAnzeige hunderter;
        private ZifferAnzeige tausender;
        public int score = 0;
    
        public static int level = 4;
    
        // Beinhaltet die werte, die sich ständig updatenden müssen
        public void updateValues(){
            
            // Verhindert nach einem Gameover die ausführung der Folgenden berechungen
            if(gameover){
                return;
            }
            
            // Füllt oder leert den Nitro "Tank" je nach dem ob das Nitro aktiviert ist oder nicht
            if (player.isBoosted)
            {
                if (nitro > 0)
                    nitro = nitro - 2;
            }
            else
            {
                if (nitro < 300)
                    nitro++;
            }
            
            // Berechung des Scores jede Sekunde
            if ((gametime.getTime() - lastUpdateTime) > 1000000)
            {
                // Score berechnung in Zonen je 164px hoch Eingeteilt und Punkteverteilung dementsprechend angepasst
                int diff = 821 - player.getLocation().y;
                int neu = diff / 164;
                score = score + neu;
                
                // Erhöhe das Level (das Tempo der Autos) nach ereichen eines bestimmten Scores
                if (score > 2560)
                    level = 10;
                else if (score > 1280)
                    level = 9;
                else if (score > 640)
                    level = 8;
                else if (score > 320)
                    level = 7;
                else if (score > 160)
                    level = 6;
                else if (score > 80)
                    level = 5;
                    
                lastUpdateTime = gametime.getTime();
            }
    
            // Sind wir gestorben?
            if (lastDeathTime != 0) {
                // Zeigt den durchsichtigen Sprite des Autos
                player.show(1);
    
                // Sind wir seit 5s tod?
                if ((gametime.getTime() - lastDeathTime) > 5000000)
                {   
                    // Wenn ja dann stelle die Kollision wieder her, setze den Timer zurück und wechsel den Sprite zum Normalen Auto zurück
                    player.setActorCollisionEnabled(true);
                    lastDeathTime = 0;
                    player.show(0);
                }
            }
        }
        
        // Sorgt dafür das die Methoden durchgehend aufgerufen werden
        public void act(){
            updateValues();
            updateNitrobar();
            updateLeben();
            updateScore();
        }
        
        // Updatet die Nitro-Anzeige
        private void updateNitrobar(){nitrobar.setValue(nitro);}
        
        // Wird aufgerufen wenn ein Neustart des Spieles nach einem Gameover stattfindet
        // Setzt alle Werte auf die Standart Werte zurück und Spielt einen Sound ab
        public void restart(){
            gameover = false;
            gameoveranzeige.hide();
            player.show();
            leben = 3;
            score = 0;
            nitro = 300;
            level = 4;
            lastDeathTime = gametime.getTime();
            playSound("wav/button.wav");
        }
        
        // Versteckt den Spieler, zeigt das Gameover Banner und setzt den gameover Wert auf wahr
        public void setGameover(){
            gameover = true;
            player.hide();
            gameoveranzeige.show();
        }
        
        // Verändert den Sprite der Lebensanzeige jenachdem wie viele Leben der Spieler noch besitzt
        private void updateLeben(){lebensanzeige.show(leben);}
    
        // Zeigt den Score des Spielers auf der Punkteanzeige an
        private void updateScore(){
            // Sobalt der Score die 9999 übersteigt wird die Punkteanzeige nicht mehr Aktualisiert da nicht mehr Punkte dagestllt werden können
            if(score > 9999){
                return;}
                
            // Setzt die Maximale länge des Strings auf 4 Stellen ließt die Zahlen von dem int Wert aus, 
            // setzt diese in den String und ersetzt die Fehlenden Stellen durch 0en (zB. _ _ 1 8 wird zu 0 0 18)
            String value = String.valueOf(score);
            String zeroPad = "0000";
            String strWPad = zeroPad.substring(value.length()) + value;
            
            // Zeigt die passenden Sprites der Actor zu den passenden Score
            einer.show(Character.getNumericValue(strWPad.charAt(3)));
            zehner.show(Character.getNumericValue(strWPad.charAt(2)));
            hunderter.show(Character.getNumericValue(strWPad.charAt(1)));
            tausender.show(Character.getNumericValue(strWPad.charAt(0)));
        }
    
        public AutoDodge()
        {
    
            super(600, 820, 1, null, "sprites/road.png", false);
            setSimulationPeriod(20);
            player = new Auto(this);
            
            // Erstellt eine ProgressBar und passt diese an, die als die Anzeige für den Tankstand des Nitros benutzt wird
            nitrobar = new GGProgressBar(this , new Location(525, 464),120, 30);
            nitrobar.setMin(0.0);
            nitrobar.setMax(300.0);
            nitrobar.setBgColor(new Color(0, 0, 0));
            nitrobar.setStripColor(new Color(178, 0, 255));
            nitrobar.setFrameColor(new Color(178, 0, 255));
            nitrobar.setTextColor(new Color(0,0,0,0));
            
            // Erstellt den Actor der als Lebensanzeige dient
            lebensanzeige = new LebensAnzeige();
            addActor(lebensanzeige, new Location(525, 185));
    
            // Erstellt die 4 Actor für die 4 stellige Punkteanzeige
            einer = new ZifferAnzeige();
            zehner = new ZifferAnzeige();
            hunderter = new ZifferAnzeige();
            tausender = new ZifferAnzeige();
    
            addActor(einer, new Location(570, 325));
            addActor(zehner, new Location(540, 325));
            addActor(hunderter, new Location(510, 325));
            addActor(tausender, new Location(480, 325));
    
            // Fügt den Spieler hinzu und gibt diesem eine Kollisionsbox
            addActor(player, new Location(225, 800), Location.NORTH);
    
            player.setCollisionRectangle(new Point(0, 0), 30, 30);
    
            // Erstellt die 10 entgegenkommenden Autos mit Sprite und Kollisionsbox
            Car[] cars = new Car[10];
            for (int i = 0; i < 10; i++)
            {
                cars[i] = new Car("sprites/car" + i + ".png");
                cars[i].setHorzMirror(true);
                cars[i].setCollisionRectangle(new Point(0, 0), 42, 82);
                player.addCollisionActor(cars[i]);
            }
            
            // Sorgt dafür das der Spieler mit den Autos sowie die Autos untereinander Kollidieren können
            for (int i = 0; i < 10; i++)
            {
                for (int j = 0; j < 10; j++)
                {
                    if (i != j)
                        cars[i].addCollisionActor(cars[j]);
                }
            }
            
            // Wenn die Autos miteinander zusammenstoßen (bei dem Spawnen) werden sie auseinander gezogen, einer um 30x nach rechts und einer um 30x nach links
            int x = 20;
            for (int i = 0; i < 5; i++)
            {
                addActor(cars[i], new Location(x, 90), Location.SOUTH);
                x += 30;
            }
    
            for (int i = 5; i < 10; i++)
            {
                addActor(cars[i], new Location(x, 350), Location.SOUTH);
                x -= 30;
            }
            
            // Fügt einen KeyListener für den Spieler Hinzu
            addKeyListener(player);
            addKeyRepeatListener(player);
    
            setTitle("AutoDodge -- Benutze die Pfeiltasten um das Auto zu bewegen - Shift für Nitro");
            
            // Erstellt den Gameoveranzeigen Actor und versteckt diesen
            gameoveranzeige = new GameoverAnzeige(this);
            addActor(gameoveranzeige, new Location(225, 410));
            gameoveranzeige.hide();       
            
            // Fügt ein MousListener dem Gameoverbanner hinzu
            addMouseListener(gameoveranzeige, GGMouse.lPress);
    
            show();
            gametime = new LoResTimer(true);
            doRun();
            
            // Musik !!!!!
            playLoop("wav/lvl4.wav");
    
        }
    
        public static void main(String[] args)
        {
            new AutoDodge();
        }
    
    }
    
    // Die Klasse des Actors der Lebensanzeige
    class LebensAnzeige extends Actor
    {
        public LebensAnzeige(){
            super("sprites/0leben.png",
                "sprites/1leben.png",
                "sprites/2leben.png",
                "sprites/3leben.png");
            show(3);
        }
    }
    
    // Die Klasse der Gameoveranzeige
    class GameoverAnzeige extends Actor implements GGMouseListener
    {
        private AutoDodge game;
        public GameoverAnzeige(AutoDodge gameboard){
            super("sprites/gameover.png");
            game = gameboard;
        }
        
        // Wenn das Banner Angeklickt wird wird das Spiel neugestartet
        public boolean mouseEvent(GGMouse evt)
        {
            if (isVisible()){
                game.restart();
                return true;
            }
    
            return false;
        }
    }
    
    // Die Klasse der Punkteanzeigen Actor
    class ZifferAnzeige extends Actor
    {
        public ZifferAnzeige(){
            super("sprites/zahl0.png",
                "sprites/zahl1.png",
                "sprites/zahl2.png",
                "sprites/zahl3.png",
                "sprites/zahl4.png",
                "sprites/zahl5.png",
                "sprites/zahl6.png",
                "sprites/zahl7.png",
                "sprites/zahl8.png",
                "sprites/zahl9.png"
            );
            show(0);
        }
    }
    
    class Car extends Actor
    {
    
        public Car(String imagePath)
        {
            super(imagePath);
        }
        // sorgt dafür das wenn die autos unten aus dem bildschirm fahren, nach oben zu einer zufälligen x koordinate 
        //teleportiert werden
        public void act()
        {   
            move(AutoDodge.level);
            if (getLocation().y > 830){
                setLocation(new Location((int)(Math.random() * 400 +10), 
                        (int)(Math.random() * -800)));
            }
        }
    
        // Wenn die Autos miteinander zusammenstoßen (bei dem Spawnen) werden sie zu einer neuen zufälligen 
        // Koordinate teleportiert
        public int collide(Actor actor1, Actor actor2)
        {
            if(actor1 == this || actor2 == this)
            { 
                if (actor1 == this)
                {
                    setLocation(new Location((int)(Math.random() * 400 + 10), 
                            (int)(Math.random() * -250)));
                }            
            }
            return 0;
        }
    }
    
    class Auto extends Actor implements GGKeyRepeatListener, GGKeyListener
    {
        private AutoDodge game;
        public boolean isBoosted = false;
        private int boostwert = 3;
        private int gamespeed = 2;
    
        public Auto(AutoDodge gameboard)
        {
            super("sprites/ACTOR.png","sprites/ACTOR2.png");
            game = gameboard;
        }
    
        // Setzt den istBoosted boolean auf true sobalt SHIFT gedrückt wird
        public boolean keyPressed(java.awt.event.KeyEvent evt)
        {
            if (evt.getKeyCode() == KeyEvent.VK_SHIFT)
                isBoosted = true;
    
            return true;
        }
    
        // Setzt den istBoosted boolean auf false sobalt SHIFT losgelassen wird.
        public boolean keyReleased(java.awt.event.KeyEvent evt)
        {
            if (evt.getKeyCode() == KeyEvent.VK_SHIFT)
                isBoosted = false;
    
            return true;
        }
    
        // Die Funktion zur bewegung des Spielers sowie die Regelung der Geschwindigkeit 
        public void keyRepeated(int keyCode)
        {
            int speed = gamespeed;
            if (isBoosted == true)
            {
                if (game.nitro > 0)
                {
                    speed = gamespeed * boostwert;
                }
            }
    
            switch (keyCode)
            {
                case KeyEvent.VK_UP:
                case KeyEvent.VK_W:
                if(getLocation().y -5 >= 10)
                {
                    setLocation(new Location(getLocation().x, getLocation().y - speed));
    
                }
                break;
    
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_S:
                if(getLocation().y +5 <= 810)
                {
                    setLocation(new Location(getLocation().x, getLocation().y + speed));
    
                }
                break;
    
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_A:
                if(getLocation().x -5 >= 15)
                {
                    setLocation(new Location(getLocation().x - speed, getLocation().y));
    
                }
                break;
    
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_D:
                if(getLocation().x +5 <= 430)
                {
                    setLocation(new Location(getLocation().x + speed, getLocation().y));
                }
                break;            
            }
    
        }
        
        // Bei einem Zusammenstoß von dem Spieler mit einem Auto, wird der Spieler unverwundbar, ihm wird ein Leben abgezogen
        // und wenn er dann kein Leben mehr haben sollt wird das GameOver eingeleitet
        public int collide(Actor actor1, Actor actor2)
        {
            gameGrid.playSound(GGSound.BOING);
    
            setActorCollisionEnabled(false);
            game.lastDeathTime = game.gametime.getTime();
            game.leben--;
            if (game.leben == 0)
                game.setGameover();
            return 0;
    
        }
    
    }
    
