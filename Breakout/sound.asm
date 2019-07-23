sid = $d400 ; 54272 in decimal

bat_sound_h = 16
bat_sound_l = 195
tile_sound_1_h = 18
tile_sound_1_l = 209
tile_sound_2_h = 18
tile_sound_2_l = 219
tile_sound_3_h = 18
tile_sound_3_l = 229
;tile_sound_2_h = 21
;tile_sound_2_l = 31
;tile_sound_3_h = 22
;tile_sound_3_l = 96

sound_1_state = $02c5
sound_2_state = $02c6
sound_3_state = $02c7
next_sound = $02c8

initialize_sound:
; clear sid registers
  lda #$00
  sta sound_1_state
  sta sound_2_state
  sta sound_3_state
  ldx #24
- sta sid,x
  dex
  bpl -
; set attack/decay
  lda #$09
  sta sid+5
  sta sid+5+7
  sta sid+5+7+7
; set sustain/release  
  lda #$a0
  sta sid+6
  sta sid+6+7
  sta sid+6+7+7
; set volume to maximum
  lda #15
  sta sid+24
  rts
  
select_next_sound:
  lda next_sound
  cmp #2
  beq +
  inc next_sound
  rts
+ lda #0
  sta next_sound
  rts
  
reset_sound: ; release all notes
  ldx sound_1_state
  beq reset_sound_2
  dex
  stx sound_1_state
  bne reset_sound_2
  lda #16
  sta sid+4
reset_sound_2:
  ldx sound_2_state
  beq reset_sound_3
  dex
  stx sound_2_state
  bne reset_sound_3
  lda #16
  sta sid+4+7
reset_sound_3:
  ldx sound_3_state
  beq reset_sound_return
  dex
  stx sound_3_state
  bne reset_sound_return
  lda #16
  sta sid+4+7+7
reset_sound_return:
  rts
  
tiles_sound:
  lda next_sound
  cmp #0
  bne +
  lda #tile_sound_1_l ; low frequency, voice 1
  sta sid
  lda #tile_sound_1_h ; high frequency, voice 1
  sta sid+1
  lda #17
  sta sid+4 ; play note voice 1
  lda #4
  sta sound_1_state
  jsr select_next_sound
  rts
+ cmp #1
  bne +
  lda #tile_sound_2_l ; low frequency, voice 2
  sta sid+7
  lda #tile_sound_2_h ; high frequency, voice 2
  sta sid+1+7
  lda #17
  sta sid+4+7 ; play note voice 2
  lda #4
  sta sound_2_state
  jsr select_next_sound
  rts
+ cmp #2
  bne +
  lda #tile_sound_3_l ; low frequency, voice 3
  sta sid+7+7
  lda #tile_sound_3_h ; high frequency, voice 3
  sta sid+1+7+7
  lda #17
  sta sid+4+7+7 ; play note voice 3
  lda #4
  sta sound_3_state
  jsr select_next_sound
+ rts
  
bat_sound:
;  lda next_sound
;  cmp #0
;  bne +
  lda #bat_sound_l ; low frequency, voice 1
  sta sid
  lda #bat_sound_h ; high frequency, voice 1
  sta sid+1
  lda #17
  sta sid+4 ; play note voice 1
  lda #4
  sta sound_1_state
;  jsr select_next_sound
  rts
;+ cmp #1
;  bne + 
;  lda #bat_sound_l ; low frequency, voice 2
;  sta sid+7
;  lda #bat_sound_h ; high frequency, voice 2
;  sta sid+1+7
;  lda #17
;  sta sid+4+7 ; play note voice 2
;  lda #5
;  sta sound_2_state
;  jsr select_next_sound
;  rts
;+ cmp #2
;  bne +
;  lda #bat_sound_l ; low frequency, voice 3
;  sta sid+7+7
;  lda #bat_sound_h ; high frequency, voice 3
;  sta sid+1+7+7
;  lda #17
;,  sta sid+4+7+7 ; play note voice 3
;  lda #5
;  sta sound_3_state
;  jsr select_next_sound
;+ rts

play_game_over:
  lda #<sample
  sta $FB
  lda #>sample
  sta $FC
  ldy #0
test_loop_end:
  ; test for end
  lda $FC
  cmp #>sample_end
  bne play_loop
  cpy #$7E ; sample_end - sample lower byte
  bne play_loop
  rts
play_loop:
  lda ($FB),y
  lsr
  lsr
  lsr
  lsr
  sta $D418 ; play sample nybble
  ldx #25  ; controls pitch/speed
- dex
  bne -
  iny
  bne test_loop_end
  inc $FC
  bne test_loop_end
  rts
  
screen_off:
  lda control_register_1
  and #%11101111
  sta control_register_1
  rts

screen_on:
  lda control_register_1
  ora #%00010000
  sta control_register_1
  rts

  