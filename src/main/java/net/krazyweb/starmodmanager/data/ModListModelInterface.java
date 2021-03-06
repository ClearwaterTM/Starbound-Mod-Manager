package net.krazyweb.starmodmanager.data;

import java.nio.file.Path;
import java.util.List;

import javafx.concurrent.Task;

public interface ModListModelInterface extends Observable {
	
	public Task<Void> getLoadTask();
	public Task<Void> getNewModsTask();
	
	public void addMods(final List<Path> files);
	
	public void deleteMod(final Mod mod);
	public Task<Void> getInstallModTask(final Mod mod);
	public void uninstallMod(final Mod mod);
	public void hideMod(final Mod mod);
	public void moveMod(final Mod mod, final int amount);
	public void reinstallConflictingMods(final List<Mod> oldOrder);
	
	public List<Mod> getMods();
	public List<Mod> getInstalledMods();
	public int indexOf(final Mod mod);
	
	public void setLocked(final boolean locked);
	public boolean isLocked();
	
	public void setModList(final List<Mod> list) ;
	
}