package org.herac.tuxguitar.util;

public class TGLock {

  private Object lock = new Object();
  private Thread lockThread = null;

  public boolean isLocked() {
    return isLocked(Thread.currentThread());
  }

  public boolean isLocked(Thread thread) {
    synchronized (this.lock) {
      return (this.lockThread != null && this.lockThread != thread);
    }
  }

  public void lock() {
    Thread thread = Thread.currentThread();

    boolean lockSuccess = false;

    synchronized (this.lock) {
      if ((lockSuccess = !this.isLocked(thread))) {
        this.lockThread = thread;
      }
    }

    if (!lockSuccess) {
      while (isLocked(thread)) {
        Thread.yield();
      }
      this.lock();
    }
  }

  public void unlock() {
    synchronized (this.lock) {
      this.lockThread = null;
    }
  }
}
