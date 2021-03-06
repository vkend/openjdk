/*
 * Copyright 2002-2007 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 */

// Forward declarations of classes defined here

class WorkGang;
class GangWorker;
class YieldingFlexibleGangWorker;
class YieldingFlexibleGangTask;
class WorkData;

// An abstract task to be worked on by a gang.
// You subclass this to supply your own work() method
class AbstractGangTask: public CHeapObj {
public:
  // The abstract work method.
  // The argument tells you which member of the gang you are.
  virtual void work(int i) = 0;

  // Debugging accessor for the name.
  const char* name() const PRODUCT_RETURN_(return NULL;);
  int counter() { return _counter; }
  void set_counter(int value) { _counter = value; }
  int *address_of_counter() { return &_counter; }

  // RTTI
  NOT_PRODUCT(virtual bool is_YieldingFlexibleGang_task() const {
    return false;
  })

private:
  NOT_PRODUCT(const char* _name;)
  // ??? Should a task have a priority associated with it?
  // ??? Or can the run method adjust priority as needed?
  int _counter;

protected:
  // Constructor and desctructor: only construct subclasses.
  AbstractGangTask(const char* name) {
    NOT_PRODUCT(_name = name);
    _counter = 0;
  }
  virtual ~AbstractGangTask() { }
};


// Class AbstractWorkGang:
// An abstract class representing a gang of workers.
// You subclass this to supply an implementation of run_task().
class AbstractWorkGang: public CHeapObj {
  // Here's the public interface to this class.
public:
  // Constructor and destructor.
  AbstractWorkGang(const char* name, bool are_GC_threads);
  ~AbstractWorkGang();
  // Run a task, returns when the task is done (or terminated).
  virtual void run_task(AbstractGangTask* task) = 0;
  // Stop and terminate all workers.
  virtual void stop();
public:
  // Debugging.
  const char* name() const;
protected:
  // Initialize only instance data.
  const bool _are_GC_threads;
  // Printing support.
  const char* _name;
  // The monitor which protects these data,
  // and notifies of changes in it.
  Monitor*  _monitor;
  // The count of the number of workers in the gang.
  int _total_workers;
  // Whether the workers should terminate.
  bool _terminate;
  // The array of worker threads for this gang.
  // This is only needed for cleaning up.
  GangWorker** _gang_workers;
  // The task for this gang.
  AbstractGangTask* _task;
  // A sequence number for the current task.
  int _sequence_number;
  // The number of started workers.
  int _started_workers;
  // The number of finished workers.
  int _finished_workers;
public:
  // Accessors for fields
  Monitor* monitor() const {
    return _monitor;
  }
  int total_workers() const {
    return _total_workers;
  }
  bool terminate() const {
    return _terminate;
  }
  GangWorker** gang_workers() const {
    return _gang_workers;
  }
  AbstractGangTask* task() const {
    return _task;
  }
  int sequence_number() const {
    return _sequence_number;
  }
  int started_workers() const {
    return _started_workers;
  }
  int finished_workers() const {
    return _finished_workers;
  }
  bool are_GC_threads() const {
    return _are_GC_threads;
  }
  // Predicates.
  bool is_idle() const {
    return (task() == NULL);
  }
  // Return the Ith gang worker.
  GangWorker* gang_worker(int i) const;

  void threads_do(ThreadClosure* tc) const;

  // Printing
  void print_worker_threads_on(outputStream *st) const;
  void print_worker_threads() const {
    print_worker_threads_on(tty);
  }

protected:
  friend class GangWorker;
  friend class YieldingFlexibleGangWorker;
  // Note activation and deactivation of workers.
  // These methods should only be called with the mutex held.
  void internal_worker_poll(WorkData* data) const;
  void internal_note_start();
  void internal_note_finish();
};

class WorkData: public StackObj {
  // This would be a struct, but I want accessor methods.
private:
  bool              _terminate;
  AbstractGangTask* _task;
  int               _sequence_number;
public:
  // Constructor and destructor
  WorkData() {
    _terminate       = false;
    _task            = NULL;
    _sequence_number = 0;
  }
  ~WorkData() {
  }
  // Accessors and modifiers
  bool terminate()                       const { return _terminate;  }
  void set_terminate(bool value)               { _terminate = value; }
  AbstractGangTask* task()               const { return _task; }
  void set_task(AbstractGangTask* value)       { _task = value; }
  int sequence_number()                  const { return _sequence_number; }
  void set_sequence_number(int value)          { _sequence_number = value; }

  YieldingFlexibleGangTask* yf_task()    const {
    return (YieldingFlexibleGangTask*)_task;
  }
};

// Class WorkGang:
class WorkGang: public AbstractWorkGang {
public:
  // Constructor
  WorkGang(const char* name, int workers, bool are_GC_threads);
  // Run a task, returns when the task is done (or terminated).
  virtual void run_task(AbstractGangTask* task);
};

// Class GangWorker:
//   Several instances of this class run in parallel as workers for a gang.
class GangWorker: public WorkerThread {
public:
  // Constructors and destructor.
  GangWorker(AbstractWorkGang* gang, uint id);

  // The only real method: run a task for the gang.
  virtual void run();
  // Predicate for Thread
  virtual bool is_GC_task_thread() const;
  // Printing
  void print_on(outputStream* st) const;
  virtual void print() const { print_on(tty); }
protected:
  AbstractWorkGang* _gang;

  virtual void initialize();
  virtual void loop();

public:
  AbstractWorkGang* gang() const { return _gang; }
};

// A class that acts as a synchronisation barrier. Workers enter
// the barrier and must wait until all other workers have entered
// before any of them may leave.

class WorkGangBarrierSync : public StackObj {
protected:
  Monitor _monitor;
  int     _n_workers;
  int     _n_completed;

  Monitor* monitor()       { return &_monitor; }
  int      n_workers()     { return _n_workers; }
  int      n_completed()   { return _n_completed; }

  void     inc_completed() { _n_completed++; }

public:
  WorkGangBarrierSync();
  WorkGangBarrierSync(int n_workers, const char* name);

  // Set the number of workers that will use the barrier.
  // Must be called before any of the workers start running.
  void set_n_workers(int n_workers);

  // Enter the barrier. A worker that enters the barrier will
  // not be allowed to leave until all other threads have
  // also entered the barrier.
  void enter();
};

// A class to manage claiming of subtasks within a group of tasks.  The
// subtasks will be identified by integer indices, usually elements of an
// enumeration type.

class SubTasksDone: public CHeapObj {
  jint* _tasks;
  int _n_tasks;
  int _n_threads;
  jint _threads_completed;
#ifdef ASSERT
  jint _claimed;
#endif

  // Set all tasks to unclaimed.
  void clear();

public:
  // Initializes "this" to a state in which there are "n" tasks to be
  // processed, none of the which are originally claimed.  The number of
  // threads doing the tasks is initialized 1.
  SubTasksDone(int n);

  // True iff the object is in a valid state.
  bool valid();

  // Set the number of parallel threads doing the tasks to "t".  Can only
  // be called before tasks start or after they are complete.
  void set_par_threads(int t);

  // Returns "false" if the task "t" is unclaimed, and ensures that task is
  // claimed.  The task "t" is required to be within the range of "this".
  bool is_task_claimed(int t);

  // The calling thread asserts that it has attempted to claim all the
  // tasks that it will try to claim.  Every thread in the parallel task
  // must execute this.  (When the last thread does so, the task array is
  // cleared.)
  void all_tasks_completed();

  // Destructor.
  ~SubTasksDone();
};

// As above, but for sequential tasks, i.e. instead of claiming
// sub-tasks from a set (possibly an enumeration), claim sub-tasks
// in sequential order. This is ideal for claiming dynamically
// partitioned tasks (like striding in the parallel remembered
// set scanning). Note that unlike the above class this is
// a stack object - is there any reason for it not to be?

class SequentialSubTasksDone : public StackObj {
protected:
  jint _n_tasks;     // Total number of tasks available.
  jint _n_claimed;   // Number of tasks claimed.
  jint _n_threads;   // Total number of parallel threads.
  jint _n_completed; // Number of completed threads.

  void clear();

public:
  SequentialSubTasksDone() { clear(); }
  ~SequentialSubTasksDone() {}

  // True iff the object is in a valid state.
  bool valid();

  // number of tasks
  jint n_tasks() const { return _n_tasks; }

  // Set the number of parallel threads doing the tasks to t.
  // Should be called before the task starts but it is safe
  // to call this once a task is running provided that all
  // threads agree on the number of threads.
  void set_par_threads(int t) { _n_threads = t; }

  // Set the number of tasks to be claimed to t. As above,
  // should be called before the tasks start but it is safe
  // to call this once a task is running provided all threads
  // agree on the number of tasks.
  void set_n_tasks(int t) { _n_tasks = t; }

  // Returns false if the next task in the sequence is unclaimed,
  // and ensures that it is claimed. Will set t to be the index
  // of the claimed task in the sequence. Will return true if
  // the task cannot be claimed and there are none left to claim.
  bool is_task_claimed(int& t);

  // The calling thread asserts that it has attempted to claim
  // all the tasks it possibly can in the sequence. Every thread
  // claiming tasks must promise call this. Returns true if this
  // is the last thread to complete so that the thread can perform
  // cleanup if necessary.
  bool all_tasks_completed();
};
