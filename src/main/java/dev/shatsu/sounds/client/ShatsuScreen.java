package dev.shatsu.sounds.client;

import dev.shatsu.sounds.ShatsuCatalog;
import dev.shatsu.sounds.ShatsuConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import java.util.List;
import java.util.Locale;

/** Sound browser and configuration screen. Audio preview uses real resolved sound events. */
public final class ShatsuScreen extends Screen {
    private final Screen previous;
    private String query = "";
    private String status = "";
    private int tab = 0, filter = 0, page = 0;
    private SoundInstance activePreview;
    private static final String[] TABS = {"Dashboard","Sound Library","Mixer","Presets","Diagnostics"};
    private static final String[] FILTERS = {"all","block_hit","block_break","block_place","footsteps",
        "block_other","combat","inventory","movement","environment","interaction","food","other"};

    public ShatsuScreen(Screen previous) {
        super(Component.literal("Shatsu Sounds"));
        this.previous = previous;
    }
    private Button action(int x,int y,int w,int h,String text,Runnable callback) {
        var button = Button.builder(Component.literal(text), b -> callback.run()).pos(x,y).size(w,h).build();
        return addRenderableWidget(button);
    }
    private void refresh() { clearWidgets(); init(); }
    private void tab(int index) { tab=index; page=0; refresh(); }
    @Override protected void init() {
        int x=Math.max(10,width/2-219), y=15;
        action(x,y,438,22,"Shatsu Sounds    -    Fabric 26.3",()->{});
        for (int i=0;i<TABS.length;i++) {
            int index=i;
            action(x+i*88,y+26,84,20,(tab==i?"[":"")+TABS[i]+(tab==i?"]":""),()->tab(index));
        }
        switch(tab) {
            case 0 -> dashboard(x,y+55);
            case 1 -> library(x,y+55);
            case 2 -> mixer(x,y+55);
            case 3 -> presets(x,y+55);
            case 4 -> diagnostics(x,y+55);
            default -> dashboard(x,y+55);
        }
        action(x+300,height-27,138,20,"Save and Close",this::onClose);
    }
    private void dashboard(int x,int y) {
        action(x,y,438,22,"Catalog: "+ShatsuCatalog.size()+" sound events",()->tab(1));
        action(x,y+29,438,22,"Preset: "+ShatsuConfig.current().preset,()->tab(3));
        action(x,y+58,438,22,"Main volume: "+Math.round(ShatsuConfig.current().master*100)+"%",()->tab(2));
        action(x,y+87,438,22,"Preserve vanilla mob voices",()->{});
        action(x,y+116,438,22,"Browse and preview Shatsu audio",()->tab(1));
        if (ShatsuCatalog.size()==0)
            action(x,y+145,438,22,"Sound assets have not been imported into this build yet",()->{});
    }
    private void library(int x,int y) {
        var field=new EditBox(font,x,y,270,20,Component.literal("Find a sound"));
        field.setValue(query);
        field.setResponder(v -> {query=v;page=0;});
        field.setHint(Component.literal("Search event name..."));
        addRenderableWidget(field);
        action(x+275,y,65,20,"Find",this::refresh);
        action(x+343,y,95,20,FILTERS[filter],()->{filter=(filter+1)%FILTERS.length;page=0;refresh();});
        List<ShatsuCatalog.Entry> filtered=ShatsuCatalog.all().stream().filter(e ->
            (filter==0 || FILTERS[filter].equals(e.category())) &&
            (query.isBlank() || e.id().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)))).toList();
        int perPage=Math.max(2,Math.min(6,(height-195)/30));
        int endPage=Math.max(0,(filtered.size()-1)/perPage);
        page=Math.min(page,endPage);
        for(int i=0;i<perPage;i++) {
            int index=page*perPage+i;
            if(index>=filtered.size()) break;
            var item=filtered.get(index);
            int yy=y+28+i*29;
            String id=item.id();
            String shortName=id.length()>34?id.substring(0,31)+"...":id;
            action(x,yy,247,22,shortName,()->preview(id));
            boolean enabled=!Boolean.FALSE.equals(ShatsuConfig.current().enabled.get(id));
            action(x+251,yy,51,22,enabled?"ON":"OFF",()->{
                ShatsuConfig.current().setEnabled(id,!enabled);refresh();
            });
            float value=ShatsuConfig.current().eventVolumes.getOrDefault(id,1f);
            action(x+306,yy,34,22,"-",()->{ShatsuConfig.current().setVolume(id,value-.1f);refresh();});
            action(x+344,yy,51,22,Math.round(value*100)+"%",()->preview(id));
            action(x+400,yy,38,22,"+",()->{ShatsuConfig.current().setVolume(id,value+.1f);refresh();});
        }
        int pager=Math.min(height-87,y+34+perPage*29);
        action(x,pager,101,20,"Previous",()->{page=Math.max(0,page-1);refresh();});
        action(x+106,pager,222,20,"Page "+(page+1)+"/"+(endPage+1)+" ("+filtered.size()+")",()->{});
        action(x+334,pager,104,20,"Next",()->{page=Math.min(endPage,page+1);refresh();});
        action(x,pager+24,169,19,"Stop preview",this::stopPreview);
        action(x+174,pager+24,264,19,status.isBlank()?"Select a sound to preview":status,()->{});
    }
    private void stopPreview() {
        if(activePreview!=null) {
            Minecraft.getInstance().getSoundManager().stop(activePreview);
            activePreview=null;
            status="Preview stopped";
            refresh();
        }
    }
    private void preview(String id) {
        try {
            var manager=Minecraft.getInstance().getSoundManager();
            var key=Identifier.parse(id);
            if(manager.getSoundEvent(key)==null) {
                status="Event not registered";
                refresh();return;
            }
            if(activePreview!=null) manager.stop(activePreview);
            var sound=SoundEvent.createVariableRangeEvent(key);
            activePreview=SimpleSoundInstance.forUI(sound,ShatsuConfig.current().pitch(id),.6f);
            manager.play(activePreview);
            status="Now playing: "+id;
            refresh();
        } catch(Exception ex) {
            status="Audio failed, check latest.log";
            System.err.println("[Shatsu Sounds] Preview failure for "+id+": "+ex);
            refresh();
        }
    }
    private void mixer(int x,int y) {
        var cfg=ShatsuConfig.current();
        action(x,y,438,21,"Master: "+Math.round(cfg.master*100)+"%   (click to raise 10%)",()->{
            cfg.master=ShatsuConfig.clamp(cfg.master+.1f);ShatsuConfig.save();refresh();
        });
        var cats=ShatsuConfig.CATEGORIES;
        int visible=Math.min(cats.length,Math.max(2,(height-180)/25));
        for(int i=0;i<visible;i++) {
            String name=cats[i], label=name.replace('_',' ');
            float v=cfg.categoryVolumes.getOrDefault(name,1f);
            int row=y+27+i*25;
            action(x,row,265,21,label.toUpperCase(Locale.ROOT),()->{});
            action(x+270,row,42,21,"-",()->{cfg.setCategory(name,v-.1f);refresh();});
            action(x+317,row,69,21,Math.round(v*100)+"%",()->{});
            action(x+392,row,46,21,"+",()->{cfg.setCategory(name,v+.1f);refresh();});
        }
    }
    private void presets(int x,int y) {
        String[] names={"Balanced","Soft ASMR","Crisp & Tactile","Immersive","Combat Focus","Minimal"};
        for(int i=0;i<names.length;i++) {
            String name=names[i];
            action(x,y+i*30,438,24,(name.equals(ShatsuConfig.current().preset)?"* ":"")+name,()->{
                ShatsuConfig.current().applyPreset(name);refresh();
            });
        }
    }
    private void diagnostics(int x,int y) {
        action(x,y,438,21,"Loaded Shatsu events: "+ShatsuCatalog.size(),()->{});
        String[] ids={"modmenu","clangor","sound","customblocksounds","sound_physics_remastered"};
        for(int i=0;i<ids.length;i++) {
            String mod=ids[i];
            boolean present=FabricLoader.getInstance().isModLoaded(mod);
            action(x,y+27+i*27,438,21,mod+": "+(present?"detected":"not installed"),()->{});
        }
        action(x,y+168,438,21,"Check logs for registration and missing audio",()->{});
    }
    @Override public void onClose() {
        if(activePreview!=null) Minecraft.getInstance().getSoundManager().stop(activePreview);
        ShatsuConfig.save();
        Minecraft.getInstance().gui.setScreen(previous);
    }
}
