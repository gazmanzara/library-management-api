package com.gazmanzara.library.controller;

import com.gazmanzara.library.model.Member;
import com.gazmanzara.library.repository.MemberRepository;
import com.gazmanzara.library.exception.ResourceNotFoundException;
import com.gazmanzara.library.exception.ResourceAlreadyExistsException;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberRepository memberRepository;

    public MemberController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @GetMapping
    public ResponseEntity<List<Member>> getAllMembers() {
        return ResponseEntity.ok(memberRepository.findAll());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Member>> searchMembers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email) {
        
        if (email != null) {
            return memberRepository.findByEmail(email)
                    .map(member -> ResponseEntity.ok(List.of(member)))
                    .orElse(ResponseEntity.ok(List.of()));
        }
        
        if (name != null) {
            return ResponseEntity.ok(
                memberRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name)
            );
        }
        
        return ResponseEntity.ok(memberRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Member> createMember(@Valid @RequestBody Member member) {
        // Check if email already exists
        if (memberRepository.existsByEmail(member.getEmail())) {
            throw new ResourceAlreadyExistsException("Member", "email", member.getEmail());
        }
        
        Member savedMember = memberRepository.save(member);
        
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedMember.getId())
                .toUri();
        
        return ResponseEntity.created(location).body(savedMember);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Member> getMemberById(@PathVariable Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "id", id));
        return ResponseEntity.ok(member);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Member> updateMember(@PathVariable Long id, @Valid @RequestBody Member memberRequest) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", "id", id));

        // Check if new email is different and already exists
        if (!member.getEmail().equals(memberRequest.getEmail()) && 
            memberRepository.existsByEmail(memberRequest.getEmail())) {
            throw new ResourceAlreadyExistsException("Member", "email", memberRequest.getEmail());
        }

        // Update member properties
        member.setFirstName(memberRequest.getFirstName());
        member.setLastName(memberRequest.getLastName());
        member.setEmail(memberRequest.getEmail());
        member.setPhone(memberRequest.getPhone());

        Member updatedMember = memberRepository.save(member);
        return ResponseEntity.ok(updatedMember);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        if (!memberRepository.existsById(id)) {
            throw new ResourceNotFoundException("Member", "id", id);
        }
        memberRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
} 