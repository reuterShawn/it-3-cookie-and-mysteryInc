import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { Note } from './note';
import { NoteService } from './note.service';
import { DoorBoardService } from '../doorBoard/doorBoard.service';
import { DoorBoard } from '../doorBoard/doorBoard';
import {TextFieldModule} from '@angular/cdk/text-field';



@Component({
  selector: 'app-add-note',
  templateUrl: './add-note.component.html',
  styleUrls: [ ]
})
export class AddNoteComponent implements OnInit {

  // Not necessary, apparently?
  // tslint:disable-next-line: no-input-rename
  // @Input('cdkTextareaAutosize')
  // enabled = true;

  @Input() doorBoard_id: string;

  addNoteForm: FormGroup;
  @Output() newNoteAdded = new EventEmitter();
  constructor(private fb: FormBuilder, private noteService: NoteService,
              private snackBar: MatSnackBar, private router: Router,
              private doorBoardService: DoorBoardService) {
  }

  @Input() doorBoard: DoorBoard;
  public serverFilteredDoorBoards: DoorBoard[];
  sub: string;
  name: string;
  email: string;
  building: string;
  officeNumber: string;

  add_note_validation_messages = {
    status: [
      {type: 'required', message: 'Status is required'},
      {type: 'pattern', message: 'Must be active, draft or template'}, // don't want to create a deleted message
    ],

    body: [,
      {type: 'required', message: 'Body is required'},
      {type: 'minLength', message: 'Body must no be empty'},
      {type: 'maxLength', message: 'Cannot exceed 1000 characters'}
    ],

  };


  createForms() {

    // add note form validations
    this.addNoteForm = this.fb.group({
      status: new FormControl('active', Validators.compose([
        Validators.required,
        Validators.pattern('^(active|draft|template)$'),
      ])),

      body: new FormControl('', Validators.compose([
        Validators.required,
        Validators.minLength(1),
        Validators.maxLength(1000),
      ])),

    });

  }

  ngOnInit() {
    this.createForms();
  }


  submitForm() {
    const noteToAdd: Note = this.addNoteForm.value;
    noteToAdd.doorBoardID = this.doorBoard_id;
    noteToAdd.addDate = new Date().toISOString();
    this.noteService.addNewNote(noteToAdd).subscribe(newID => {
      // Notify the DoorBoard component that a note has been added.
      this.newNoteAdded.emit();
      this.snackBar.open('Added Note ', null, {
        duration: 2000,
      });
    }, err => {
      this.snackBar.open('Failed to add the note', null, {
        duration: 2000,
      });
    });
  }

}
