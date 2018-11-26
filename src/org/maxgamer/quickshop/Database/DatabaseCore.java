package org.maxgamer.quickshop.Database;

import java.sql.Connection;

public abstract interface DatabaseCore
{
  public abstract Connection getConnection();
  
  public abstract void queue(BufferStatement paramBufferStatement);
  
  public abstract void flush();
  
  public abstract void close();
}
