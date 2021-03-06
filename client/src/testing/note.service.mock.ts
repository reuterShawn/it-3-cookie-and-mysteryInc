import { Injectable, inject } from '@angular/core';
import { Observable, of } from 'rxjs';
import { Note, SaveNote } from '../app/notes/note';
import { NoteService } from '../app/notes/note.service';

@Injectable()
export class MockNoteService extends NoteService {
  static testNotes: Note[] = [
    {
      _id: 'first_id',
      doorBoardID: 'test-id',
      body: 'This is the body of the first test id. It is somewhat long.',
      addDate: new Date(),
      expiration: '2025-03-06T22:03:38+0000',
      status: 'active',
      favorite: false,
      isExpired: false,
      isPinned: true,
    },
    {
      _id: 'second_id',
      doorBoardID: 'test-id',
      body: 'This is the second test id.',
      addDate: new Date(),
      expiration: '2025-03-06T22:03:38+0000',
      status: 'active',
      favorite: false,
      isExpired: false,
      isPinned: true,
    },
    {
      _id: 'third_id',
      doorBoardID: 'test-id',
      body: 'Third test id body.',
      addDate: new Date(),
      expiration: '2025-03-06T22:03:38+0000',
      status: 'template',
      favorite: false,
      isExpired: false,
      isPinned: true,
    },
    {
      _id: 'fourth_id',
      doorBoardID: 'test-id',
      body: 'This is the fourth test id.',
      addDate: new Date(),
      expiration: '2025-03-06T22:03:38+0000',
      status: 'template',
      favorite: false,
      isExpired: false,
      isPinned: true,
    },
    {
      _id: 'fifth_id',
      doorBoardID: 'test-id',
      body: 'Fifth id test body.',
      addDate: new Date(),
      expiration: '2025-03-06T22:03:38+0000',
      status: 'draft',
      favorite: false,
      isExpired: false,
      isPinned: true,
    },
    {
      _id: 'sixth_id',
      doorBoardID: 'test-id',
      body: 'Sixth id test body.',
      addDate: new Date(),
      expiration: '2025-03-06T22:03:38+0000',
      status: 'draft',
      favorite: false,
      isExpired: false,
      isPinned: true,
    },
    {
      _id: 'seventh_id',
      doorBoardID: 'test-id',
      body: 'Fifth id test body.',
      addDate: new Date(),
      expiration: '2025-03-06T22:03:38+0000',
      status: 'deleted',
      favorite: false,
      isExpired: false,
      isPinned: true,
    },
    {
      _id: 'eighth_id',
      doorBoardID: 'test-id',
      body: 'Eighth id test body.',
      addDate: new Date(),
      expiration: '2025-03-06T22:03:38+0000',
      status: 'deleted',
      favorite: true,
      isExpired: false,
      isPinned: true,
    }
  ];

  public static FAKE_BODY = 'This is definitely the note you wanted';

  constructor() {
    super(null);
  }

  getNotesByDoorBoard(DoorBoardID: string): Observable<Note[]> {
   let notesObtained: Note[];
   let amount = 0;
   for(let i = 0; i < 8; i++){
      if (DoorBoardID === MockNoteService.testNotes[i].doorBoardID) {
        notesObtained[amount] = MockNoteService.testNotes[i];
        amount++;
      }
    }
   return of(notesObtained);

  }

  getFavoriteNotes(DoorBoardID: string): Observable<Note[]> {
    let notesObtained: Note[];
    let amount = 0;
    for(let i = 0; i < 8; i++){
       if (DoorBoardID === MockNoteService.testNotes[i].doorBoardID && true === MockNoteService.testNotes[i].favorite) {
         notesObtained[amount] = MockNoteService.testNotes[i];
         amount++;
       }
     }
    return of(notesObtained);

   }

   filterNotes(testNotes: Note[], filters: { addDate?: Date, expireDate?: Date, favorite?: boolean, isExpired?: boolean } ): Note[] {
     let filteredNotes: Note[];
     let DoorBoardID: string;
     let amount = 0;
     for (let i = 0; i < 8; i++){
       if (DoorBoardID === MockNoteService.testNotes[i].doorBoardID && true === MockNoteService.testNotes[i].favorite &&
        false === MockNoteService.testNotes[i].isExpired) {
          filteredNotes[amount] = MockNoteService.testNotes[i];
          amount++;
         }
     }
     return filteredNotes;
  }

  getNoteByID( _id: string): Note {
    let noteToReturn;
    for(let i = 0; i < 8; i++){
      if(_id === MockNoteService.testNotes[i]._id) {
         noteToReturn = MockNoteService.testNotes[i];
    }
      return noteToReturn;
  }
}

  getNoteById(id: string): Observable<Note> {
    // If the specified ID is for the first test note,
    // return that note, otherwise return `null` so
    // we can test illegal note requests.
    if (id === MockNoteService.testNotes[0]._id) {
      return of(MockNoteService.testNotes[0]);
    } else {
      return of(null);
    }
  }

}
