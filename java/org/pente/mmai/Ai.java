package org.pente.mmai;

import java.util.*;

import org.pente.gameServer.core.AlphaNumericGridCoordinates;
import org.pente.gameServer.core.GridCoordinates;

/**
 * @author dweebo
 */
public class Ai {

	public native long init();
	public native void privateDestroy(long ptr);
	public native void toggleCallbacks(long ptr, int callbacks);

	private native void start(long ptr);
	private native void stop(long ptr);
	private native int move(long ptr, int moves[], int game, int level, int vct);

	static {
		System.loadLibrary("Ai");
	}

	private Thread thread;
	private volatile boolean running;
	private volatile boolean destroyed;
	private long cPtr;
	private int game;
	private int level = 1;
	private int vct;
	private int seat = 1;
	private long treeId;

	private boolean active;

	private List<AiListener> aiListeners = new ArrayList<AiListener>();

	public Ai() {
		cPtr = init();
	}
	public Ai(int game, int level, int vct, int seat, long treeId) {
		cPtr = init();
		this.game = game;
		this.level = level;
		this.vct = vct;
		this.seat = seat;
		this.treeId = treeId;
	}
	public void addAiListener(AiListener aiListener) {
		aiListeners.add(aiListener);
	}

	public int getSeat() {
		return seat;
	}

	public void destroy() {
		stopThinking();
		System.out.println("destroyed flag set");

		// if thread is still alive in getMove call, then allow it
		// to finish and destroy from there.  otherwise can crash
		if (thread == null || !thread.isAlive()) {
			System.out.println("thread not alive, java destroy");
			privateDestroy(cPtr);
		}
		else {
			destroyed = true;
		}
	}

	public void stopThinking() {
		if (running) {
			running = false;
			stop(cPtr);
			thread.interrupt();
		}
		notifyStopThinking();
	}
	private void notifyStopThinking() {
		for (AiListener aiListener : aiListeners) {
			aiListener.stopThinking();
		}
	}

	public void setVisualization(boolean visualization) {
		toggleCallbacks(cPtr, visualization ? 1 : 0);
	}

	public void getMove(final int moves[]) {
		startThinking();
		running = true;
		start(cPtr);
		thread = new Thread(new Runnable() {
			public void run() {

				int newMove = move(cPtr, moves, game, level, vct);
				if (running) {
					for (AiListener aiListener : aiListeners) {
						aiListener.moveReady(moves, newMove);
					}
					notifyStopThinking();
				}
				if (destroyed) {
					System.out.println("destroy from getMove() java");
					privateDestroy(cPtr);
				}
			}
		});
		thread.start();
	}
	private void aiEvaluatedCallBack() {
		for (AiListener aiListener : aiListeners) {
			aiListener.aiEvaluateCallBack();
		}
	}
	private void aiVisualizationCallBack(int bd[]) {
		for (AiListener aiListener : aiListeners) {
			aiListener.aiVisualizationCallBack(bd);
		}
	}
	private void startThinking() {
		for (AiListener aiListener : aiListeners) {
			aiListener.startThinking();
		}
	}

    private static final GridCoordinates coordinates =
		new AlphaNumericGridCoordinates(19, 19);
	public static void main(String args[]) {
		final Ai ai = new Ai(1, 1, 1, 1, -1);
		ai.addAiListener(new AiListener() { /*,184,181,199,182,200,183 */
		//ai.getMove(new int[] { 180,181,237,238,199,256,218 }, new AiListener() {
			public void moveReady(int[] moves, int newMove) {
				System.out.println("returned move " + newMove+" " +coordinates.getCoordinate(newMove));

//				ai.getMove(new int[] { 180,181,237,238,199,256,218,newMove }, new AiListener() {
//				public void moveReady(int[] moves, int newMov2) {
//					System.out.println("returned move " + newMov2+" " + coordinates.getCoordinate(newMov2));
//					ai.destroy();
//				}
//			});
			}
			public void aiVisualizationCallBack(int[] bd) {
				for (int i=0;i<19;i++){
					for(int j=0;j<19;j++) {
						int x = bd[i*19+j];
						if (x == -1) x = 0;
						System.out.print(x + " ");
					}
					System.out.println();
				}
				System.out.println();
			}
			public void aiEvaluateCallBack() {
			}
			public void startThinking() {
			}
			public void stopThinking() {
			}
		});

		ai.getMove(new int[] { 180,184,181});
		System.out.println("after calling getmove");
		//try { Thread.sleep(1000); } catch (InterruptedException e) {}
		//ai.stopThinking();
		//System.out.println("after calling stop");

	}
	public long getTreeId() {
		return treeId;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public void setVct(int vct) {
		this.vct = vct;
	}
	public void setSeat(int seat) {
		this.seat = seat;
	}
	public void setTreeId(long treeId) {
		this.treeId = treeId;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public void setGame(int game) {
		this.game = game;
	}
	public int getLevel() {
		return level;
	}
	public int getVct() {
		return vct;
	}
}
